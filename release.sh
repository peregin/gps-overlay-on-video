#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Release stopped: sbt-release requires a clean checkout. Commit or stash these changes before retrying:" >&2
  git status --short >&2
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required to create the release." >&2
  exit 1
fi

if ! gh auth status --active --hostname github.com; then
  echo "Release stopped: GitHub CLI authentication failed. Run 'gh auth login -h github.com'; if GH_TOKEN or GITHUB_TOKEN is set, replace or unset an invalid value before retrying." >&2
  exit 1
fi

# "with-defaults" releases the snapshot version without its -SNAPSHOT suffix.
declared_version="$(sed -nE 's/^[[:space:]]*(ThisBuild[[:space:]]*\/[[:space:]]*)?version[[:space:]]*:=[[:space:]]*"([0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?)"[[:space:]]*$/\2/p' version.sbt)"
if [[ -z "$declared_version" || "$declared_version" == *$'\n'* ]]; then
  echo "Release stopped: version.sbt must contain exactly one numeric major.minor.patch version, such as 'ThisBuild / version := \"3.0.0-SNAPSHOT\"'." >&2
  exit 1
fi
if [[ "$declared_version" != *-SNAPSHOT ]]; then
  echo "Release stopped: sbt-release requires a snapshot version. Change version.sbt from '$declared_version' to '$declared_version-SNAPSHOT', then retry." >&2
  exit 1
fi
release_version="${declared_version%-SNAPSHOT}"

version_is_newer() {
  local new_major new_minor new_patch old_major old_minor old_patch
  IFS=. read -r new_major new_minor new_patch <<< "$1"
  IFS=. read -r old_major old_minor old_patch <<< "$2"
  (( 10#$new_major > 10#$old_major ||
     (10#$new_major == 10#$old_major && 10#$new_minor > 10#$old_minor) ||
     (10#$new_major == 10#$old_major && 10#$new_minor == 10#$old_minor && 10#$new_patch > 10#$old_patch) ))
}

# Query the remote, since local tags may be stale or incomplete.
if ! remote_tags="$(git ls-remote --refs --tags origin 'refs/tags/v*')"; then
  echo "Release stopped: could not read tags from the origin remote. Check your network connection and 'git remote -v', then retry." >&2
  exit 1
fi
while IFS=$'\t' read -r _ ref; do
  [[ -n "${ref:-}" ]] || continue
  existing_version="${ref#refs/tags/v}"
  if [[ ! "$existing_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Cannot compare existing remote tag $ref; release cancelled." >&2
    exit 1
  fi
  if ! version_is_newer "$release_version" "$existing_version"; then
    echo "Release stopped: v$release_version is not newer than existing remote tag v$existing_version. Increase the version in version.sbt before releasing." >&2
    exit 1
  fi
done <<< "$remote_tags"

if [[ -n "$(git tag --list "v$release_version")" ]]; then
  echo "Release stopped: local tag v$release_version already exists. Choose a newer version in version.sbt." >&2
  exit 1
fi

release_log="$(mktemp "${TMPDIR:-/tmp}/gps-overlay-release.XXXXXX")"
set +e
sbt --server "release with-defaults" 2>&1 | tee "$release_log"
release_status=("${PIPESTATUS[@]}")
set -e
if (( release_status[0] != 0 || release_status[1] != 0 )); then
  echo "Release stopped: the sbt release step failed (sbt exit ${release_status[0]}). The relevant errors are:" >&2
  error_summary="$(grep -E '\[error\]|fatal:|Exception' "$release_log" | tail -n 15 || true)"
  if [[ -n "$error_summary" ]]; then
    printf '%s\n' "$error_summary" >&2
  else
    tail -n 15 "$release_log" >&2
  fi
  echo "Full sbt output: $release_log" >&2
  echo "If sbt reached its tag or push steps, inspect 'git status' and 'git tag --list' before retrying." >&2
  exit 1
fi

tag="$(git tag --list 'v*' --points-at HEAD^)"
if [[ "$tag" != "v$release_version" ]]; then
  echo "Release stopped before GitHub publishing: expected tag v$release_version on the commit before HEAD; found: $tag. Inspect the sbt log at $release_log and the recent git commits." >&2
  exit 1
fi

asset="target/release/gps-overlay-on-video.jar"
if [[ ! -f "$asset" ]]; then
  echo "Release stopped before GitHub publishing: sbt did not produce $asset. Inspect the assembly output in $release_log." >&2
  exit 1
fi

if ! gh release create "$tag" "$asset" \
  --repo github.com/peregin/gps-overlay-on-video \
  --verify-tag \
  --title "$tag" \
  --notes "Release $tag"; then
  echo "The tag was pushed, but GitHub release creation failed. Fix the gh error above, then create the release for $tag with $asset; do not rerun the sbt release step." >&2
  exit 1
fi
