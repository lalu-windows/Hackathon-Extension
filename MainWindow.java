import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow {

	// the row builder
	public static JPanel subrow(String subject, int attended, int total,Runnable onDelete){
		int percentage = (int)(((double) attended / total) * 100);

		JPanel subPanel = new JPanel(new BorderLayout(10,10));
		subPanel.setPreferredSize(new Dimension(600,25));
		subPanel.setMaximumSize(new Dimension(600,25));
		subPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel subjectName = new JLabel(subject);
		subjectName.setPreferredSize(new Dimension(130,25));
		subjectName.setFont(new Font("Arial", Font.BOLD, 15));

		JProgressBar bar = new JProgressBar(0,100);
		bar.setValue(percentage);
		bar.setStringPainted(true);
		bar.setString(percentage + "%");

		JLabel ratio = new JLabel(attended + "/" + total);
		ratio.setPreferredSize(new Dimension(60,25));

		subPanel.add(subjectName, BorderLayout.WEST);
		subPanel.add(bar, BorderLayout.CENTER);
	JButton delbtn = new JButton("X");
delbtn.setMargin(new Insets(0,6,0,6));
delbtn.addActionListener(e -> onDelete.run());

JPanel east = new JPanel(new BorderLayout(5,0));
east.add(ratio, BorderLayout.CENTER);
east.add(delbtn, BorderLayout.EAST);

subPanel.add(east, BorderLayout.EAST);	
		return subPanel;
	}

	// ---------- Attendance page ----------
	static class AttenGUI extends JPanel {

		// data and the panel that holds the rows are fields so refresh() can reach them
		private final List<String> subjects = new ArrayList<>();
private final List<Integer> attended = new ArrayList<>();
private final List<Integer> tot = new ArrayList<>();
private final JComboBox<String> subjectdrop = new JComboBox<>();
private final JPanel subjectPanel = new JPanel();
				public AttenGUI(){
			setLayout(new BorderLayout(10,10));
			setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

			JLabel titlelabel = new JLabel("Attendance tracker", JLabel.CENTER);
			titlelabel.setFont(new Font("Inter", Font.BOLD, 25));
			add(titlelabel, BorderLayout.NORTH);

			subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));
			add(subjectPanel, BorderLayout.CENTER);

			add(buildUpdatePanel(), BorderLayout.SOUTH);
			for(Object[] r : DBManager.loadAll()){
    				subjects.add((String) r[0]);
			        attended.add((Integer) r[1]);
			        tot.add((Integer) r[2]);
			        subjectdrop.addItem((String) r[0]);
			}

			JPanel bottom = new JPanel(new GridLayout(2,1));
			bottom.add(buildAddPanel());
			bottom.add(buildUpdatePanel());
			add(bottom, BorderLayout.SOUTH);
			
			
			
			
			refresh(); // first draw
		}

		// redraws subjectpanel
		public void refresh(){
			subjectPanel.removeAll();

			subjectPanel.add(Box.createVerticalGlue());
			for(int i = 0; i < subjects.size(); i++){
    final int idx = i; // lambdas need a final copy of i
    subjectPanel.add(subrow(subjects.get(i), attended.get(i), tot.get(i), () -> deleteSubject(idx)));
    subjectPanel.add(Box.createRigidArea(new Dimension(0,8)));
}
		/*	for(int i = 0; i < subjects.size(); i++){
			
				subjectPanel.add(subrow(subjects.get(i), attended.get(i), tot.get(i),));
				subjectPanel.add(Box.createRigidArea(new Dimension(0,8)));
			}*/
			subjectPanel.add(Box.createVerticalGlue());
			subjectPanel.revalidate(); // re-run the layout for the new components
    subjectPanel.repaint();

		}
//adding delete button gui 
		private void deleteSubject(int index){
    String name = subjects.get(index);
    int ok = JOptionPane.showConfirmDialog(this, "Delete " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
    if(ok != JOptionPane.YES_OPTION) return;

    DBManager.delete(name);      // remove from the database first
    subjects.remove(index);      // then from the lists (index is an int, so this removes by position)
    attended.remove(index);
    tot.remove(index);
    subjectdrop.removeItemAt(index);
    refresh();
}
		//for adding subjects:
		private JPanel buildAddPanel(){
    JPanel addpan = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
    addpan.setBorder(BorderFactory.createTitledBorder("Add subject"));

    JTextField namefield = new JTextField(10);
    JTextField attfield = new JTextField(4);
    JTextField totalfield = new JTextField(4);
    JButton addbtn = new JButton("Add");

    addbtn.addActionListener(e -> {
        String name = namefield.getText().trim();
        try{
            int a = Integer.parseInt(attfield.getText().trim());
            int t = Integer.parseInt(totalfield.getText().trim());

            if(name.isEmpty() || a < 0 || t <= 0 || a > t){
                JOptionPane.showMessageDialog(this, "Enter a name and valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(subjects.contains(name)){
                JOptionPane.showMessageDialog(this, "Subject already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DBManager.insert(name, a, t);
            subjects.add(name);
            attended.add(a);
            tot.add(t);
            subjectdrop.addItem(name);
            refresh();

            namefield.setText(""); attfield.setText(""); totalfield.setText("");
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    addpan.add(new JLabel("Name: "));     addpan.add(namefield);
    addpan.add(new JLabel("Attended: ")); addpan.add(attfield);
    addpan.add(new JLabel("Total: "));    addpan.add(totalfield);
    addpan.add(addbtn);
    return addpan;
}
		// Update attendance section
		private JPanel buildUpdatePanel(){
			JPanel updatepan = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
			updatepan.setBorder(BorderFactory.createTitledBorder("Update attendance"));

			JTextField attenfield = new JTextField(4);
			JTextField totfield = new JTextField(4);
			JButton updatebtn = new JButton("Update");

			// autofill the text fields when a subject is selected
			subjectdrop.addActionListener(e -> {
				int index = subjectdrop.getSelectedIndex();
				if(index<0) return;
				attenfield.setText(String.valueOf(attended.get(index)));
				totfield.setText(String.valueOf(tot.get(index)));

			});
			if(subjectdrop.getItemCount() > 0) subjectdrop.setSelectedIndex(0);

			updatebtn.addActionListener(e -> {
				int index = subjectdrop.getSelectedIndex();
				try{
					int atten = Integer.parseInt(attenfield.getText().trim());
					int newtot = Integer.parseInt(totfield.getText().trim());

					if(atten < 0 || newtot <= 0){
						JOptionPane.showMessageDialog(this, "Values must be positive", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(atten > newtot){
						JOptionPane.showMessageDialog(this, "Attended cannot be greater than total", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					attended.set(index,atten);
				tot.set(index,newtot);
				DBManager.update(subjects.get(index),atten,newtot);
	


					refresh(); // redraw with the new numbers
				}
				catch(NumberFormatException ex){
					JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			});

			updatepan.add(new JLabel("Subject: "));
			updatepan.add(subjectdrop);
			updatepan.add(new JLabel("Attended: "));
			updatepan.add(attenfield);
			updatepan.add(new JLabel("Total: "));
			updatepan.add(totfield);
			updatepan.add(updatebtn);

			return updatepan;
		}
	}

	// // grade gui
static class GradeGUI extends JPanel {

	private final List<String> subjects = new ArrayList<>();
	private final List<Integer> marks = new ArrayList<>();
	private final List<Integer> tot = new ArrayList<>();
	private final JComboBox<String> subjectdrop = new JComboBox<>();
	private final JPanel subjectPanel = new JPanel();

	public GradeGUI(){
		setLayout(new BorderLayout(10,10));
		setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

		JLabel titlelabel = new JLabel("Grade tracker", JLabel.CENTER);
		titlelabel.setFont(new Font("SansSerif", Font.BOLD, 25));
		add(titlelabel, BorderLayout.NORTH);

		subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));
		add(subjectPanel, BorderLayout.CENTER);

		for(Object[] r : DBManager.loadAllGrades()){
			subjects.add((String) r[0]);
			marks.add((Integer) r[1]);
			tot.add((Integer) r[2]);
			subjectdrop.addItem((String) r[0]);
		}

		JPanel bottom = new JPanel(new GridLayout(2,1));
		bottom.add(buildAddPanel());
		bottom.add(buildUpdatePanel());
		add(bottom, BorderLayout.SOUTH);

		refresh(); // first draw
	}

	// redraws subjectpanel
	public void refresh(){
		subjectPanel.removeAll();

		subjectPanel.add(Box.createVerticalGlue());
		for(int i = 0; i < subjects.size(); i++){
			final int idx = i;
			subjectPanel.add(subrow(subjects.get(i), marks.get(i), tot.get(i), () -> deleteSubject(idx)));
			subjectPanel.add(Box.createRigidArea(new Dimension(0,8)));
		}
		subjectPanel.add(Box.createVerticalGlue());

		subjectPanel.revalidate();
		subjectPanel.repaint();
	}

	// Add subject section
	private JPanel buildAddPanel(){
		JPanel addpan = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
		addpan.setBorder(BorderFactory.createTitledBorder("Add subject"));

		JTextField namefield = new JTextField(10);
		JTextField marksfield = new JTextField(4);
		JTextField totalfield = new JTextField(4);
		JButton addbtn = new JButton("Add");

		addbtn.addActionListener(e -> {
			String name = namefield.getText().trim();
			try{
				int m = Integer.parseInt(marksfield.getText().trim());
				int t = Integer.parseInt(totalfield.getText().trim());

				if(name.isEmpty() || m < 0 || t <= 0 || m > t){
					JOptionPane.showMessageDialog(this, "Enter a name and valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(subjects.contains(name)){
					JOptionPane.showMessageDialog(this, "Subject already exists", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				DBManager.insertGrade(name, m, t);
				subjects.add(name);
				marks.add(m);
				tot.add(t);
				subjectdrop.addItem(name);
				refresh();

				namefield.setText(""); marksfield.setText(""); totalfield.setText("");
			}catch(NumberFormatException ex){
				JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		addpan.add(new JLabel("Name: "));   addpan.add(namefield);
		addpan.add(new JLabel("Marks: "));  addpan.add(marksfield);
		addpan.add(new JLabel("Total: "));  addpan.add(totalfield);
		addpan.add(addbtn);
		return addpan;
	}

	// Update marks section
	private JPanel buildUpdatePanel(){
		JPanel updatepan = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
		updatepan.setBorder(BorderFactory.createTitledBorder("Update marks"));

		JTextField marksfield = new JTextField(4);
		JTextField totfield = new JTextField(4);
		JButton updatebtn = new JButton("Update");

		// autofill the text fields when a subject is selected
		subjectdrop.addActionListener(e -> {
			int index = subjectdrop.getSelectedIndex();
			if(index < 0) return;
			marksfield.setText(String.valueOf(marks.get(index)));
			totfield.setText(String.valueOf(tot.get(index)));
		});
		if(subjectdrop.getItemCount() > 0) subjectdrop.setSelectedIndex(0);

		updatebtn.addActionListener(e -> {
			int index = subjectdrop.getSelectedIndex();
			if(index < 0) return;
			try{
				int m = Integer.parseInt(marksfield.getText().trim());
				int newtot = Integer.parseInt(totfield.getText().trim());

				if(m < 0 || newtot <= 0){
					JOptionPane.showMessageDialog(this, "Values must be positive", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(m > newtot){
					JOptionPane.showMessageDialog(this, "Marks cannot be greater than total", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				marks.set(index, m);
				tot.set(index, newtot);
				DBManager.updateGrade(subjects.get(index), m, newtot);

				refresh();
			}
			catch(NumberFormatException ex){
				JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		updatepan.add(new JLabel("Subject: "));
		updatepan.add(subjectdrop);
		updatepan.add(new JLabel("Marks: "));
		updatepan.add(marksfield);
		updatepan.add(new JLabel("Total: "));
		updatepan.add(totfield);
		updatepan.add(updatebtn);

		return updatepan;
	}

	private void deleteSubject(int index){
		String name = subjects.get(index);
		int ok = JOptionPane.showConfirmDialog(this, "Delete " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
		if(ok != JOptionPane.YES_OPTION) return;

		DBManager.deleteGrade(name);
		subjects.remove(index);
		marks.remove(index);
		tot.remove(index);
		subjectdrop.removeItemAt(index);
		refresh();
	}
}
// parent section
// overview gui: read-only, shows warnings
static class OverviewGUI extends JPanel {

	private static final int ATT_LIMIT = 75;    // attendance warning below this %
	private static final int GRADE_LIMIT = 50;  // grade warning below this % (change as you like)

	private final JPanel warnPanel = new JPanel();
	private final JPanel attPanel = new JPanel();
	private final JPanel gradePanel = new JPanel();

	public OverviewGUI(){
		setLayout(new BorderLayout(10,10));
		setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

		JLabel titlelabel = new JLabel("Overview", JLabel.CENTER);
		titlelabel.setFont(new Font("SansSerif", Font.BOLD, 25));
		add(titlelabel, BorderLayout.NORTH);

		warnPanel.setLayout(new BoxLayout(warnPanel, BoxLayout.Y_AXIS));
		warnPanel.setBorder(BorderFactory.createTitledBorder("Warnings"));

		attPanel.setLayout(new BoxLayout(attPanel, BoxLayout.Y_AXIS));
		attPanel.setBorder(BorderFactory.createTitledBorder("Attendance (min " + ATT_LIMIT + "%)"));

		gradePanel.setLayout(new BoxLayout(gradePanel, BoxLayout.Y_AXIS));
		gradePanel.setBorder(BorderFactory.createTitledBorder("Grades (min " + GRADE_LIMIT + "%)"));

		JPanel columns = new JPanel(new GridLayout(1,2,15,0));
		columns.add(attPanel);
		columns.add(gradePanel);

		JPanel center = new JPanel(new BorderLayout(10,10));
		center.add(warnPanel, BorderLayout.NORTH);
		center.add(columns, BorderLayout.CENTER);
		add(center, BorderLayout.CENTER);

		refresh(); // first draw
	}

	// reload from the DB and redraw everything
	public void refresh(){
		warnPanel.removeAll();
		attPanel.removeAll();
		gradePanel.removeAll();

		fill(attPanel, DBManager.loadAll(), ATT_LIMIT, "Attendance");
		fill(gradePanel, DBManager.loadAllGrades(), GRADE_LIMIT, "Grade");

		if(warnPanel.getComponentCount() == 0){
			JLabel ok = new JLabel("All subjects are on track.");
			ok.setForeground(new Color(46, 160, 67));
			warnPanel.add(ok);
		}

		warnPanel.revalidate(); warnPanel.repaint();
		attPanel.revalidate();  attPanel.repaint();
		gradePanel.revalidate(); gradePanel.repaint();
	}

	// builds one column and adds warnings for low subjects
	private void fill(JPanel target, java.util.List<Object[]> rows, int limit, String what){
		target.add(Box.createVerticalGlue());
		for(Object[] r : rows){
			String name = (String) r[0];
			int a = (Integer) r[1];
			int t = (Integer) r[2];
			int pct = t == 0 ? 0 : (int) Math.round(100.0 * a / t);
			boolean low = pct < limit;
			Color color = low ? new Color(207, 34, 46) : new Color(46, 160, 67);

			// row: name | bar | ratio
			JPanel row = new JPanel(new BorderLayout(10,10));
			row.setPreferredSize(new Dimension(400,25));
			row.setMaximumSize(new Dimension(400,25));

			JLabel nameLabel = new JLabel(name);
			nameLabel.setPreferredSize(new Dimension(80,25));
			nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

			JProgressBar bar = new JProgressBar(0,100);
			bar.setValue(pct);
			bar.setStringPainted(true);
			bar.setString(pct + "%");
			bar.setForeground(color);

			JLabel ratio = new JLabel(a + "/" + t);
			ratio.setPreferredSize(new Dimension(60,25));

			row.add(nameLabel, BorderLayout.WEST);
			row.add(bar, BorderLayout.CENTER);
			row.add(ratio, BorderLayout.EAST);
			target.add(row);
			target.add(Box.createRigidArea(new Dimension(0,8)));

			if(low){
				String msg = "WARNING: " + name + " " + what.toLowerCase() + " is " + pct + "% (below " + limit + "%)";
				if(what.equals("Attendance")){
					// classes to attend in a row to reach the limit: (a+n)/(t+n) >= limit/100
					int n = (int) Math.ceil((limit / 100.0 * t - a) / (1 - limit / 100.0));
					msg += ". Attend the next " + n + " classes to recover.";
				}
				JLabel warn = new JLabel(msg);
				warn.setForeground(color);
				warnPanel.add(warn);
			}
		}
		target.add(Box.createVerticalGlue());
	}
}






	// ---------- Main frame ----------
	static class MainFrame extends JFrame {
		public MainFrame(){
			setTitle("Academic Tracker");
			setSize(1280,720);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setLocationRelativeTo(null);

			CardLayout cardlay = new CardLayout();
			JPanel cardpan = new JPanel(cardlay);

			cardpan.add(new AttenGUI(), "ATTENDANCE");
			cardpan.add(new GradeGUI(), "GRADE");

			JPanel btnpan = new JPanel();
			JButton attenbtn = new JButton("Attendance");
			JButton gradebtn = new JButton("Grade");
			attenbtn.addActionListener(e -> cardlay.show(cardpan, "ATTENDANCE"));
			gradebtn.addActionListener(e -> cardlay.show(cardpan, "GRADE"));
			OverviewGUI parent = new OverviewGUI();
			cardpan.add(parent,"parent");

			JButton parbtn = new JButton("parent");
			parbtn.addActionListener(e->{
				parent.refresh();
				cardlay.show(cardpan,"parent");});


			btnpan.add(attenbtn);
			btnpan.add(gradebtn);
			btnpan.add(parbtn);

			add(btnpan, BorderLayout.NORTH);
			add(cardpan, BorderLayout.CENTER);
		}
	}


	public static void main(String[] args){
    DBManager.createTable();
    DBManager.createGradeTable();

    // seed default subjects only on the very first run
    if(DBManager.loadAll().isEmpty()){
        DBManager.insert("TOC", 35, 40);
        DBManager.insert("OOP", 20, 38);
        DBManager.insert("MATHS", 32, 40);
        DBManager.insert("DSA", 25, 35);
        DBManager.insert("DE&LD", 28, 34);
        DBManager.insert("ETHICS", 25, 35);
    }

    if(DBManager.loadAllGrades().isEmpty()){
	    DBManager.insertGrade("TOC",78,100);
            DBManager.insertGrade("OOP",98,100);
            DBManager.insertGrade("MATHS",88,100);

            DBManager.insertGrade("DSA",78,80);
	    
            DBManager.insertGrade("DE&LD",58,60);

            DBManager.insertGrade("ETHICS",58,80);
    }




    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
}
}
