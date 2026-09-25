#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required to create the release." >&2
  exit 1
fi

if ! gh auth status --active --hostname github.com >/dev/null 2>&1; then
  echo "Authenticate gh for github.com (gh auth login or GITHUB_TOKEN) before releasing." >&2
  exit 1
fi

# "with-defaults" releases the version in version.sbt without -SNAPSHOT.
release_version="$(sed -nE 's/^[[:space:]]*(ThisBuild[[:space:]]*\/[[:space:]]*)?version[[:space:]]*:=[[:space:]]*"([0-9]+\.[0-9]+\.[0-9]+)(-SNAPSHOT)?"[[:space:]]*$/\2/p' version.sbt)"
if [[ -z "$release_version" || "$release_version" == *$'\n'* ]]; then
  echo "Expected one numeric major.minor.patch version in version.sbt." >&2
  exit 1
fi

version_is_newer() {
  local new_major new_minor new_patch old_major old_minor old_patch
  IFS=. read -r new_major new_minor new_patch <<< "$1"
  IFS=. read -r old_major old_minor old_patch <<< "$2"
  (( 10#$new_major > 10#$old_major ||
     (10#$new_major == 10#$old_major && 10#$new_minor > 10#$old_minor) ||
     (10#$new_major == 10#$old_major && 10#$new_minor == 10#$old_minor && 10#$new_patch > 10#$old_patch) ))
}

# Query the remote, since local tags may be stale or incomplete.
remote_tags="$(git ls-remote --refs --tags origin 'refs/tags/v*')"
while IFS=$'\t' read -r _ ref; do
  [[ -n "${ref:-}" ]] || continue
  existing_version="${ref#refs/tags/v}"
  if [[ ! "$existing_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Cannot compare existing remote tag $ref; release cancelled." >&2
    exit 1
  fi
  if ! version_is_newer "$release_version" "$existing_version"; then
    echo "v$release_version must be newer than existing remote tag v$existing_version; release cancelled." >&2
    exit 1
  fi
done <<< "$remote_tags"

if [[ -n "$(git tag --list "v$release_version")" ]]; then
  echo "Local tag v$release_version already exists; release cancelled." >&2
  exit 1
fi

sbt "release with-defaults"

tag="$(git tag --list 'v*' --points-at HEAD^)"
if [[ "$tag" != "v$release_version" ]]; then
  echo "Expected release tag v$release_version on the commit before HEAD; found: $tag" >&2
  exit 1
fi

asset="target/release/gps-overlay-on-video.jar"
if [[ ! -f "$asset" ]]; then
  echo "Release JAR was not produced at $asset" >&2
  exit 1
fi

gh release create "$tag" "$asset" \
  --repo github.com/peregin/gps-overlay-on-video \
  --verify-tag \
  --title "$tag" \
  --notes "Release $tag"
