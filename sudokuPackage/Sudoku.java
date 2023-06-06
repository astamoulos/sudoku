package sudokuPackage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.net.*;
import java.io.*;
import java.util.Stack;  


public class Sudoku extends JFrame implements ActionListener{
    public static final int WIDTH = 400, HEIGHT = 350;
	JTextField[][] field = new JTextField[9][9];
	JTextField last = null;
	int [][] solution = null;
	JButton[] b;
	JButton erase, undo, rubik;
	JCheckBox check;
	Stack<undo> stk = null;
	String old;
	boolean start = false, undo_flag = false, undo_flag1 = false, pressed = false;
	
    public static void main(String args[]) {
        Sudoku gui = new Sudoku();
        gui.setVisible(true);
		gui.requestFocus();
    }

    public Sudoku() {
        super("Sudoku");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
		
        JMenu menu = new JMenu("New Game");
        menu.setBackground(Color.LIGHT_GRAY);

        JMenuItem Easy = new JMenuItem("Easy");
		Easy.addActionListener(this); 
        menu.add(Easy);

        JMenuItem Intermidiate = new JMenuItem("Intermidiate");
		Intermidiate.addActionListener(this); 
        menu.add(Intermidiate);

        JMenuItem Expert = new JMenuItem("Expert");
		Expert.addActionListener(this); 
        menu.add(Expert);

        JMenuBar bar = new JMenuBar();
        bar.add(menu);
        setJMenuBar(bar);

		GridLayout grid_layout = new GridLayout(3, 3);
		grid_layout.setHgap(2);
		grid_layout.setVgap(2);
		JPanel  grid = new JPanel(grid_layout);
		grid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		JPanel[]  box = new JPanel[9];
		
		for (int i = 0; i < 9; ++i) {
			box[i] = new JPanel(grid_layout);
			box[i].setBorder(BorderFactory.createRaisedBevelBorder());
			int row = i - (i % 3);
			int column = 3 * (i % 3);
			for (int j = row; j < row + 3; j++) {
				for (int k = column; k < column + 3; k++) {
		            field[j][k] = new JTextField(2);
					field[j][k].setBorder(BorderFactory.createLineBorder( new Color(87 , 160, 211), 1));
		            field[j][k].setHorizontalAlignment(JTextField.CENTER); //Center text horizontally in the text field.
					field[j][k].getDocument().addDocumentListener(new MyDocumentListener());
					field[j][k].setName(""+j+k);
					((PlainDocument)field[j][k].getDocument()).setDocumentFilter(new MyIntFilter());
					field[j][k].addFocusListener(new FocusListener(){
						public void focusGained(FocusEvent evt){
							JTextField f = (JTextField) evt.getSource();
							String str = f.getText();
							if(!str.equals("")){
								for(int i=0; i<9 ; i++){
									for(int j=0; j<9 ; j++){
										if(str.equals( field[i][j].getText()) && !field[i][j].getBackground().equals(new Color(239,48,56)))
											field[i][j].setBackground(new Color(255, 255, 200));
									}
								}
							}
							else{
								f.setBackground(new Color(255, 255, 200));
							}
							last = f;
							old = last.getText();
						}
						public void focusLost(FocusEvent evt){
							for(int i=0; i<9 ; i++){
								for(int j=0; j<9 ; j++){
									if(field[i][j].getBackground().equals(new Color(255, 255, 200))){
										if(field[i][j].isEditable())
											field[i][j].setBackground( Color.WHITE);
										else
											field[i][j].setBackground(new Color(229, 229, 229));	
									}
								}
							}
							old = last.getText();
							collisions();
							mistakes();
						}
					});
		            box[i].add(field[j][k]);
	        	}
        	}
            grid.add(box[i]);
        }
		this.add(grid);

		JPanel panel = new JPanel();  
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		b = new JButton[10];
		for(int i=0; i <9; i++){
			b[i] = new JButton( String.valueOf(i+1) ); 
			b[i].addActionListener(this); 
			b[i].setRequestFocusEnabled(false);
			panel.add(b[i]);
		}

		ImageIcon icon = new ImageIcon("Images/eraser.png");
		Image image = icon.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT);
  		icon = new ImageIcon(image);
		erase = new JButton(icon);
		erase.setPreferredSize( b[0].getPreferredSize() );
		erase.setActionCommand("erase");
		erase.addActionListener(this); 
		erase.setRequestFocusEnabled(false);
		panel.add(erase);

		ImageIcon undo_icon = new ImageIcon("Images/undo.png");
		Image undo_image = undo_icon.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT);
  		undo_icon = new ImageIcon(undo_image);
		undo = new JButton(undo_icon);
		undo.setPreferredSize( b[0].getPreferredSize());
		undo.setActionCommand("undo");
		undo.addActionListener(this); 
		undo.setRequestFocusEnabled(false);
		panel.add(undo);
		
		check =  new JCheckBox("Verify against solution");
		check.setFont(check.getFont().deriveFont((float)11));
		check.setPreferredSize(new Dimension(183, 23) );
		check.setHorizontalAlignment(JTextField.CENTER);
		check.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED && solution != null){
					if(old != null)
						collisions();
					mistakes();
				}
				else if (e.getStateChange() == ItemEvent.DESELECTED){
					for(int i = 0; i < 9; i++){
						for(int j = 0; j < 9; j++){
							if(field[i][j].getBackground().equals(new Color(100, 149, 237)))
								field[i][j].setBackground(Color.WHITE);	
						}
					}
				}
			}
		});
		panel.add(check);
		
		ImageIcon rubik_icon = new ImageIcon("Images/rubik.png");
		Image rubik_image = rubik_icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
  		rubik_icon = new ImageIcon(rubik_image);
		rubik = new JButton(rubik_icon);
		rubik.setPreferredSize(b[0].getPreferredSize());
		rubik.setActionCommand("rubik");
		rubik.addActionListener(this);
		panel.add(rubik);
		
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		this.add(panel);
    }

	
	public void actionPerformed(ActionEvent e) {
	    String str = e.getActionCommand();
		URL url = null; 
		System.out.println("button "+str);
		if(last != null && str.matches("[1-9]") && last.isEditable() ){
			//if(stk != null && last != null)
			//	stk.push( new undo(last, last.getText()));
			pressed = true;
			undo_flag = false;
			undo_flag1 = false;
			old = last.getText(); 
			last.setText(str); 
			collisions();
			mistakes();
			if(is_solved()){
				System.out.println("Congrats");
				control_buttons(false);
				JOptionPane.showMessageDialog(this, "              Congrats You won!", "", JOptionPane.PLAIN_MESSAGE);
			}
			return;
		}
		else if(str.equals("erase")){
			if(last != null && last.isEditable()){
			//	stk.push(new undo(last, last.getText() ));
				old = last.getText();
				last.setText("");
				collisions();
				mistakes();
			}
			return;
		}
		else if(str.equals("rubik")){
			start = false;
			requestFocus();
			for(int i = 0; i < 9; i++){
				for(int j = 0; j < 9; j++){
					if(field[i][j].getBackground().equals(new Color(100, 149, 237)))
						field[i][j].setBackground(Color.WHITE);	
					else if(field[i][j].getBackground().equals(new Color(239,48,56))){
						if(field[i][j].isEditable())
							field[i][j].setBackground(Color.WHITE);	
						else
							field[i][j].setBackground(new Color(229,229,229));
					}
				}
			}
			if(solution != null){
				for(int i = 0; i < 9; i++){
					for(int j = 0; j < 9; j++){
						field[i][j].setText(""+solution[i][j]); 
					}	
				}
				control_buttons(false);
				JOptionPane.showMessageDialog(this, "              Congrats You won!", "", JOptionPane.PLAIN_MESSAGE);
			}
			stk = new Stack<undo>(); 
			return;
		}
		else if(str.equals("undo")){
			if(stk == null || stk.empty())
				return;
			undo_flag = true;
			undo_flag1 = true;
			System.out.println("Elements in Stack: " + stk);
			undo u = stk.pop();
			
			System.out.println("Elements in Stack: " + stk); 
			last = u.j;
			old = last.getText();
			u.j.setText("");
			last.requestFocus();
			u.j.setText(u.str);
			collisions();
			mistakes();
			undo_flag = false;
			undo_flag1 = false;
			return;
		}
		else if( !str.equals("Easy") && !str.equals("Intermidiate") && !str.equals("Expert"))
			return;
		try{
		    if(str.equals("Easy") ) {
				url = new URL("http://gthanos.inf.uth.gr/~gthanos/sudoku/exec.php?difficulty=easy");
		    }
		    else if(str.equals("Intermidiate") ) {
				url = new URL("http://gthanos.inf.uth.gr/~gthanos/sudoku/exec.php?difficulty=intermediate");
		    }
		    else if(str.equals("Expert") ) {
				url = new URL("http://gthanos.inf.uth.gr/~gthanos/sudoku/exec.php?difficulty=expert");
		    }
			/*
			URLConnection urlcon = url.openConnection();
			urlcon.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader( url.openStream()));
			*/
			
			BufferedReader in = new BufferedReader(new FileReader("test/test1"));
			String inputLine;
			solution = new int[9][9];
			for(int i = 0; i < 9; i++){
				inputLine = in.readLine();
				for(int j = 0; j < 9; j++){
					if(inputLine.charAt(j) != '.'){
						field[i][j].setText(""+inputLine.charAt(j)); 
						field[i][j].setEditable(false);
						field[i][j].setBackground(new Color(229, 229, 229));
						solution[i][j] = Character.getNumericValue(inputLine.charAt(j));
					}
					else{
						field[i][j].setText(""); 	
						field[i][j].setBackground(Color.WHITE);
					}
				}	
			}
			in.close();
			solver s = new solver(solution);
			s.solve();
			control_buttons(true);
			stk = new Stack<undo>(); 
			start = true;
			pressed = false;
		}
		catch(MalformedURLException ex) {
	      System.out.println("Malformed URL");
	      ex.printStackTrace();
	    }
		catch(IOException ex) {
	    	System.out.println("Error while reading or writing from URL: "+url.toString() );
	    }	
  	}

	class MyDocumentListener implements DocumentListener {
 		
        public void insertUpdate(DocumentEvent e) {
			if(start){
				System.out.println("hi1");
				if(stk != null && last != null && !undo_flag){
					System.out.println("push: "+last.getName()+" "+old);
					stk.push( new undo(last, old));
				}
				collisions();
				mistakes();
				old = last.getText();
				is_solved();
				undo_flag = false;
				undo_flag1 = false;
			}
        }
        public void removeUpdate(DocumentEvent e) {
			if(start  && !pressed){
				System.out.println("hi2");
				if(stk != null && last != null && !undo_flag1){
					stk.push( new undo(last, old));
					System.out.println("push : "+last.getText()+" "+old);
				}
				collisions();
				mistakes();
				old = last.getText();
				undo_flag1 = false;
			}
			pressed = false;
        }
        public void changedUpdate(DocumentEvent e) {
            System.out.println("hi3");
        }
    }
	
	private void control_buttons(Boolean state){
		check.setEnabled(state);
		erase.setEnabled(state);
		undo.setEnabled(state);
		rubik.setEnabled(state);
		for(int i = 0; i < 9; i++)
			b[i].setEnabled(state);
	}

	private void mistakes(){
		if(!check.isSelected())
			return;
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				String str = field[i][j].getText(); 
				if( !str.isEmpty() && Integer.valueOf(str) != solution[i][j] && !field[i][j].getBackground().equals(new Color(239,48,56)))
					field[i][j].setBackground(new Color(100, 149, 237));
				else if(!str.isEmpty() && Integer.valueOf(str) == solution[i][j] && field[i][j].getBackground().equals(new Color(100, 149, 237)))
					field[i][j].setBackground(Color.WHITE);
				else if(str.isEmpty()  && field[i][j].getBackground().equals(new Color(100, 149, 237)) )
					field[i][j].setBackground(Color.WHITE);
			}
		}
	}

	public void reset(int r, int c, String num){
		if(num == null || num.equals(""))
			return;
		int counter = 0;
		for(int i = 0; i < 9; i++){
			if(num.equals(field[r][i].getText()) && field[r][i].getBackground().equals(new Color(239,48,56)) ){
				if(!field[r][i].isEditable())
					field[r][i].setBackground(new Color(229, 229, 229));
				else
					field[r][i].setBackground(Color.WHITE);
				if(!last.equals(field[r][i]) )
					counter++;
			}	
		}	
		if(counter > 1){
			for(int i = 0; i < 9; i++){
				if(num.equals(field[r][i].getText()) && !last.equals(field[r][i])){
					field[r][i].setBackground(new Color(239,48,56));
				}	
			}		
		}
		counter = 0;
		
		for(int i = 0; i < 9; i++){
			if(num.equals(field[i][c].getText()) && field[i][c].getBackground().equals(new Color(239,48,56)) ){
				if(!field[i][c].isEditable())
					field[i][c].setBackground(new Color(229, 229, 229));
				else
					field[i][c].setBackground(Color.WHITE);
				if(!last.equals(field[i][c]))
					counter++;
			}	
		}	
		if(counter > 1){
			for(int i = 0; i < 9; i++){
				if(num.equals(field[i][c].getText()) && !last.equals(field[i][c])){
					field[i][c].setBackground(new Color(239,48,56));
				}	
			}		
		}
		counter = 0;

		int boxi = r - (r % 3);
		int boxj = c - (c % 3);
		for(int i = boxi; i < boxi + 3; i++){
			for(int j = boxj; j < boxj + 3; j++){
				if(num.equals(field[i][j].getText()) && field[i][j].getBackground().equals(new Color(239,48,56)) ){
					if(!field[i][j].isEditable())
						field[i][j].setBackground(new Color(229, 229, 229));
					else
						field[r][i].setBackground(Color.WHITE);
					if(!last.equals(field[i][j]) )
						counter++;
				}
			}
		}

		if(counter > 1){
			for(int i = boxi; i < boxi + 3; i++){
				for(int j = boxj; j < boxj + 3; j++){
					if(num.equals(field[i][j].getText()) && !last.equals(field[i][j])){
						field[i][j].setBackground(new Color(239,48,56));
					}	
				}
			}	
		}
		
		if(!last.isEditable())
			last.setBackground(new Color(229, 229, 229));
		else
			last.setBackground(Color.WHITE);
	}

	public void not_in_row(int r, String num){
		if(num.equals(""))
			return;
		
		int count = 0;
		for(int i = 0; i < 9; i++){
			if(num.equals( field[r][i].getText()) && field[r][i].equals(last) == false){
				field[r][i].setBackground(new Color(239,48,56));
				count++;
			}
		}
		if(count > 0)
			last.setBackground(new Color(239,48,56));
		
		return;
	}

	public void not_in_collumn(int c, String num){
		if(num.equals(""))
			return;
		int count = 0;
		for(int i = 0; i < 9; i++){
			if(num.equals( field[i][c].getText()) && field[i][c].equals(last) == false){
				field[i][c].setBackground(new Color(239,48,56));
				count++;
			}
		}
		if(count > 0)
			last.setBackground(new Color(239,48,56));
			
		return ; 
	}

	public void not_in_box(int r, int c, String num){
		if(num.equals(""))
			return;
		int count = 0;
		int boxi = r - (r % 3);
		int boxj = c - (c % 3);
		for(int i = boxi; i < boxi + 3; i++){
			for(int j = boxj; j < boxj + 3; j++){
				if(num.equals( field[i][j].getText()) && field[i][j].equals(last) == false){
					field[i][j].setBackground(new Color(239,48,56));
					count++;
				}	
			}
		}
		if(count > 0)
			last.setBackground(new Color(239,48,56));
			
		return ; 
	}

	public void collisions(){
		String s = last.getName();
		reset(Character.getNumericValue(s.charAt(0)), Character.getNumericValue(s.charAt(1)), old);
		not_in_row(Character.getNumericValue(s.charAt(0)), last.getText());
		not_in_collumn(Character.getNumericValue(s.charAt(1)), last.getText());
		not_in_box(Character.getNumericValue(s.charAt(0)), Character.getNumericValue(s.charAt(1)), last.getText());
	}

	public boolean is_solved(){
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				String str = field[i][j].getText(); 
				if(str.equals("") || Integer.valueOf(str) != solution[i][j])
					return false;
			}
		}
		return true;
	}		
}

class MyIntFilter extends DocumentFilter {
	@Override
	public void insertString(FilterBypass fb, int offset, String string,
		AttributeSet attr) throws BadLocationException {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.insert(offset, string);

		System.out.println("F1");
		if (test(sb.toString())) {
			super.insertString(fb, offset, string, attr);
		} else {
			// warn the user and don't allow the insert
		}
	}
	/*
	private boolean test(String text) {
		int intValue;
		if(text.equals(""))
			return true;
		if(text.length() > 1)	
			return false;
		try {
	        intValue = Integer.parseInt(text);
	        return true && intValue != 0;
	    } catch (NumberFormatException e) {
	        System.out.println("Input String cannot be parsed to Integer.");
			return false;
	    }
	}
	*/
	private boolean test(String text) {
		return text.matches("[1-9]|^$");
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text,
		AttributeSet attrs) throws BadLocationException {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.replace(offset, offset + length, text);

		System.out.println("F2");
		if (test(sb.toString())) {
			super.replace(fb, offset, length, text, attrs);
		} else {
			// warn the user and don't allow the insert
		}

	}

	@Override
	public void remove(FilterBypass fb, int offset, int length)
	throws BadLocationException {
		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.delete(offset, offset + length);

		System.out.println("F3");
		if (test(sb.toString())) {
			super.remove(fb, offset, length);
		} else {
			// warn the user and don't allow the insert
		}
	
	}
}