package study;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.PlainDocument;


/**
 * Combobox which lists all installed fonts, sorted alphabetically.
 * In the dropdown, each font name is shown in the default font
 * together with some characters in its own font, which can be
 * customized calling the <code>setPreviewString</code> method.
 * 
 * In the main text field, the default font is used to display the font name.
 * It is editable and supports auto completion.
 * 
 * The last <code>n</code> selected fonts can be shown on the top
 * by calling <code>setRecentFontsCount(n)</code>.
 * 
 * This file is public domain. However, if you improve it, please
 * share your work with andi@xenoage.com. Thanks!
 * 
 * @author Andreas Wenger
 */
public class FontChooserComboBox
	extends JComboBox
	implements ItemListener
{
	
	private int previewFontSize;
	private String previewString = "AaBbCc";
	private int recentFontsCount = 5;
	
	private List<String> fontNames;
	private HashMap<String, Item> itemsCache = new HashMap<String, Item>();
	private LinkedList<String> recentFontNames;
	private HashMap<String, Item> recentItemsCache = new HashMap<String, Item>();
	
	
	/**
	 * Creates a new {@link FontChooserComboBox}.
	 */
	public FontChooserComboBox()
	{
		//load available font names
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();
    Arrays.sort(fontNames);
    this.fontNames = Arrays.asList(fontNames);
    
    //recent fonts
    recentFontNames = new LinkedList<String>();
    
    //fill combo box
    JLabel label = new JLabel();
    this.previewFontSize = label.getFont().getSize();
    updateList(null);  
    
    //set editor and item components
    this.setEditable(true);
		this.setEditor(new FontChooserComboBoxEditor());
		this.setRenderer(new FontChooserComboBoxRenderer());
		
		//listen to own item changes
		this.addItemListener(this);
	}
	
	
	/**
	 * Gets the font size of the preview characters.
	 */
	public int getPreviewFontSize()
	{
		return previewFontSize;
	}
	
	
	/**
	 * Sets the font size of the preview characters.
	 */
	public void setPreviewFontSize(int previewFontSize)
	{
		this.previewFontSize = previewFontSize;
		updateList(getSelectedFontName());
	}
	
	
	/**
	 * Gets the preview characters, or null.
	 */
	public String getPreviewString()
	{
		return previewString;
	}
	
	
	/**
	 * Sets the preview characters, or the empty string or null to
	 * display no preview but only the font names.
	 */
	public void setPreviewString(String previewString)
	{
		this.previewString = (previewString != null && previewString.length() > 0 ?
			previewString : null);
		updateList(getSelectedFontName());
	}
	
	
	/**
	 * Gets the number of recently selected fonts, or 0.
	 */
	public int getRecentFontsCount()
	{
		return recentFontsCount;
	}
	
	
	/**
	 * Sets the number of recently selected fonts, that are shown
	 * on the top of the list, or 0 to hide them.
	 */
	public void setRecentFontsCount(int recentFontsCount)
	{
		this.recentFontsCount = recentFontsCount;
		boolean listChanged = false;
		while (recentFontNames.size() > recentFontsCount)
		{
			recentFontNames.removeLast();
			listChanged = true;
		}
		if (listChanged)
			updateList(getSelectedFontName());
	}
	
	
	public void itemStateChanged(ItemEvent e)
	{
		//remember current font in list of recent fonts
		String fontName = getSelectedFontName();
		if (fontName != null && recentFontsCount > 0 &&
			!(recentFontNames.size() > 0 && (recentFontNames.getFirst().equals(fontName))))
		{
			//remove occurrence in list
			recentFontNames.remove(fontName);
			//add at first position
			recentFontNames.addFirst(fontName);
			//trim list
			if (recentFontNames.size() > recentFontsCount)
				recentFontNames.removeLast();
			updateList(fontName);
		}
	}
	
		
	private void updateList(String selectedFontName)
	{
		//list items
		removeAllItems();
		itemsCache.clear();
		recentItemsCache.clear();
		//recent fonts
		if (recentFontNames.size() > 0)
		{
			for (String recentFontName : recentFontNames)
			{
				Item item = new Item(recentFontName);
	    	addItem(item);
	    	recentItemsCache.put(recentFontName, item);
			}
    	addItem(new Item(null)); //separator
		}
		//regular items
		for (String fontName : fontNames)
    {
			Item item = new Item(fontName);
    	addItem(item);
    	itemsCache.put(fontName, item);
    }
		//reselect item
		if (selectedFontName != null)
			setSelectedItem(selectedFontName);
	}
	
	
	/**
	 * Gets the selected font name, or null.
	 */
	public String getSelectedFontName()
	{
		if (this.getSelectedItem() != null)
			return ((Item) this.getSelectedItem()).font.getFontName();
		else
			return null;
	}
	

	@Override public Dimension getPreferredSize()
	{
		//default height: like a normal combo box
		return new Dimension(super.getPreferredSize().width, new JComboBox().getPreferredSize().height);
	}
	
	
	/**
	 * Sets the selected font by the given name.
	 * If it does not exist, nothing happens.
	 */
	public void setSelectedItem(String fontName)
	{
		//if a string is given, find the corresponding font, otherwise do nothing
		Item item = recentItemsCache.get(fontName); //first in recent items
		if (item == null)
			item = itemsCache.get(fontName); //then in regular items
		if (item != null)
			setSelectedItem(item);
	}
	
	
	/**
	 * The editor component of the list.
	 * This is an editable text area which supports auto completion.
	 * 
	 * @author Andreas Wenger
	 */
	class FontChooserComboBoxEditor
		extends BasicComboBoxEditor 
	{
		

		/**
		 * Plain text document for the text area.
		 * Needed for text selection.
		 * 
		 * Inspired by http://www.java2s.com/Code/Java/Swing-Components/AutocompleteComboBox.htm
		 * 
		 * @author Andreas Wenger
		 */
		class AutoCompletionDocument
			extends PlainDocument
		{
			
			private JTextField textField = FontChooserComboBoxEditor.this.editor;
			
			@Override public void replace(int i, int j, String s, AttributeSet attributeset)
				throws BadLocationException
			{
				super.remove(i, j);
				insertString(i, s, attributeset);
			}


			@Override public void insertString(int i, String s, AttributeSet attributeset)
				throws BadLocationException
			{
				if (s != null && !"".equals(s))
				{
					String s1 = getText(0, i);
					String s2 = getMatch(s1 + s);
					int j = (i + s.length()) - 1;
					if (s2 == null)
					{
						s2 = getMatch(s1);
						j--;
					}
					if (s2 != null)
						FontChooserComboBox.this.setSelectedItem(s2);
					super.remove(0, getLength());
					super.insertString(0, s2, attributeset);
					textField.setSelectionStart(j + 1);
					textField.setSelectionEnd(getLength());
				}
			}


			@Override public void remove(int i, int j) throws BadLocationException
			{
				int k = textField.getSelectionStart();
				if (k > 0)
					k--;
				String s = getMatch(getText(0, k));

				super.remove(0, getLength());
				super.insertString(0, s, null);

				if (s != null)
					FontChooserComboBox.this.setSelectedItem(s);
				try
				{
					textField.setSelectionStart(k);
					textField.setSelectionEnd(getLength());
				}
				catch (Exception exception)
				{
				}
			}

		}


		private FontChooserComboBoxEditor()
		{
			editor.setDocument(new AutoCompletionDocument());
			if (fontNames.size() > 0)
				editor.setText(fontNames.get(0).toString());
		}
		

		private String getMatch(String input)
		{
			for (String fontName : fontNames)
			{
				if (fontName.toLowerCase().startsWith(input.toLowerCase()))
					return fontName;
			}
			return null;
		}


		public void replaceSelection(String s)
		{
			AutoCompletionDocument doc = (AutoCompletionDocument) editor.getDocument();
			try
			{
				Caret caret = editor.getCaret();
				int i = min(caret.getDot(), caret.getMark());
				int j = max(caret.getDot(), caret.getMark());
				doc.replace(i, j - i, s, null);
			}
			catch (BadLocationException ex)
			{
			}
		}

	}
	
	
	/**
	 * The renderer for a list item.
	 * 
	 * @author Andreas Wenger
	 */
	class FontChooserComboBoxRenderer
		implements ListCellRenderer
	{
		
		public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus)
		{
			//extract the component from the item's value
			Item item = (Item) value;
			boolean s = (isSelected && !item.isSeparator);
			item.setBackground(s ? list.getSelectionBackground() : list.getBackground());
			item.setForeground(s ? list.getSelectionForeground() : list.getForeground());
			return item;
		}
	}
	
	
	
	/**
	 * The component for a list item.
	 * 
	 * @author Andreas Wenger
	 */
	class Item
		extends JPanel
	{
	
		private final Font font;
		private final boolean isSeparator;
		
		
		private Item(String fontName)
		{
			if (fontName != null)
			{
				this.font = new Font(fontName, Font.PLAIN, previewFontSize);
				this.isSeparator = false;
			}
			else
			{
				this.font = null;
				this.isSeparator = true;
			}
			
			this.setOpaque(true);
			
			if (!isSeparator)
			{
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				
				//font name in default font
				JLabel labelHelp = new JLabel(font.getName());
				this.add(labelHelp);
				
				//preview string in this font
				if (previewString != null)
				{
					//show only supported characters
					StringBuilder thisPreview = new StringBuilder();
					for (int i = 0; i < previewString.length(); i++)
					{
						char c = previewString.charAt(i);
						if (font.canDisplay(c))
							thisPreview.append(c);
					}
					JLabel labelFont = new JLabel(thisPreview.toString());
					labelFont.setFont(font);
					this.add(labelFont);
				}
			}
			else
			{
				//separator
				this.setLayout(new BorderLayout());
				this.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.CENTER);
			}
		}
		
		
		@Override public String toString()
		{
			if (font != null)
				return font.getFamily();
			else
				return "";
		}
		
	}


}
