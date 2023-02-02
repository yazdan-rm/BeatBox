import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;



public class BeatBox implements Serializable {
    JPanel mainPanelForCheckbox;
    ArrayList<JCheckBox> checkBoxList;
     transient Sequencer sequencer;
     transient Sequence sequence;
    Track track;
    JFrame frame;
    Box buttonBox;
    ImageIcon uncheckedBox=new ImageIcon("icons8-unchecked-checkbox-64.png");
    ImageIcon checkedBox=new ImageIcon("icons8-checked-checkbox-64.png");
    ImageIcon imageIcon=new ImageIcon("music-note.png");
    String[] instrumentNames={"Bass Drum","Closed Hi_Hat","Open Hi_Hat","Acoustic Snare","Crash Cymbal",
                          "Hand Clap","High Tom","Hi Bongo","Maracas","Whistle","Low Conga","Cowbell",
                          "Vibraslap","Low-mid Tom","High Agogo","Open Hi Conga" };
    int[] instruments={35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
    public void buildGui() {
        frame = new JFrame("Cyber BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxList = new ArrayList<>();

         buttonBox=new Box(BoxLayout.Y_AXIS);
       // buttonBox.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JButton start = new JButton("Start");
        start.setFocusable(false);
        start.addActionListener(e -> buildTrackAndStart());
        buttonBox.add(start);

        JButton stop=new JButton("Stop");
        stop.setFocusable(false);
        stop.addActionListener(e -> sequencer.stop());
        buttonBox.add(stop);

        JButton upTemp=new JButton("Tempo Up");
        upTemp.setFocusable(false);
        upTemp.addActionListener(e -> {
            float tempoFactor=sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*1.03));
        });
        buttonBox.add(upTemp);

        JButton downTemp=new JButton("Tempo Down");
        downTemp.setFocusable(false);
        downTemp.addActionListener(e -> {
            float tempoFactor=sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*0.97));
        });
        buttonBox.add(downTemp);



        JPanel nameBox=new JPanel();
        nameBox.setLayout(new BoxLayout(nameBox,BoxLayout.Y_AXIS));
        for (int i = 0; i < 16; i++) {
            Label a= new Label(instrumentNames[i]);
            a.setForeground(Color.LIGHT_GRAY);
            nameBox.add(a);


        }
        backgroundPanel.add(BorderLayout.EAST,buttonBox);
        backgroundPanel.add(BorderLayout.WEST,nameBox);

        frame.add(backgroundPanel);

        mainPanelForCheckbox=new JPanel();
        mainPanelForCheckbox.setLayout(new GridLayout(16,16,1,1));
        backgroundPanel.add(BorderLayout.CENTER,mainPanelForCheckbox);

        mainPanelForCheckbox.setBackground(new Color(0xC5DBCA));
        nameBox.setBackground(new Color(0x12563C));

        for (int i = 0; i < 256; i++) {
            JCheckBox c=new JCheckBox();
            c.setIcon(uncheckedBox);
            c.setSelectedIcon(checkedBox);
            c.setSelected(false);
            checkBoxList.add(c);                //create a copy from each checkbox and save it .
            mainPanelForCheckbox.add(c);
        }

        JButton cleanButton=new JButton("Clean All Check Box");
        cleanButton.setFocusable(false);
        cleanButton.addActionListener(e -> {
            for (int i = 0; i < 256; i++) {
                checkBoxList.get(i).setSelected(false);   //use checkbox list array for retrieve checkboxes and
                                                          //set selected option to false .
            }
        });

        buttonBox.add(cleanButton);

        JButton saveButton=new JButton("Save");
        saveButton.setFocusable(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser=new JFileChooser();
                fileChooser.setCurrentDirectory(new File("C:\\Users\\Asus\\Desktop"));
                int response=fileChooser.showSaveDialog(frame);
                if(response==JFileChooser.APPROVE_OPTION)
                {
                    File file=new File(fileChooser.getSelectedFile().getAbsolutePath());
                    serialized(file);
                }
            }
        });
        buttonBox.add(saveButton);

        JButton restoreButton=new JButton("Restore");
        restoreButton.setFocusable(false);
        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser=new JFileChooser();
                fileChooser.setCurrentDirectory(new File("C:\\Users\\Asus\\Desktop"));
                int response=fileChooser.showOpenDialog(frame);
                if(response==JFileChooser.APPROVE_OPTION)
                {
                    File file=new File(fileChooser.getSelectedFile().getAbsolutePath());
                    deSerialized(file);
                }
            }
        });
        buttonBox.add(restoreButton);

        frame.setIconImage(imageIcon.getImage());
        frame.setBounds(90,20,1300,700);
        //frame.pack();     //I prefer to use setBounds method ;
        frame.setVisible(true);

        setUpMidi();
    }

    public void setUpMidi()
    {
      try{
          sequencer= MidiSystem.getSequencer();
          sequencer.open();
          sequence=new Sequence(Sequence.PPQ,4);
          sequencer.setTempoInBPM(120);
      }catch (Exception e)
      {
          e.printStackTrace();
      }
    }

    public void buildTrackAndStart()
    {
        int[]trackList;

        track=sequence.createTrack();

        for (int i = 0; i < 16; i++) {

            trackList=new int[16];

            int key=instruments[i];

            for (int j = 0; j < 16 ; j++) {
                JCheckBox jc= checkBoxList.get(j+(16*i));
                if(jc.isSelected())
                {
                    trackList[j]=key;
                }else{
                    trackList[j]=0;
                }
            }
            makeTrack(trackList);   // collect sound notes and send them for making track.
        }

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);  //make a loop and play sound repetitively.
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void makeTrack(int[] list)
    {
        for (int i = 0; i < 16; i++) {
            int key =list[i];

            if(key!=0)
            {
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100,i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comand, int chan, int one, int two, int tick)
    {
        MidiEvent event=null;
        try{
            ShortMessage a=new ShortMessage();
            a.setMessage(comand,chan,one,two);
            event=new MidiEvent(a,tick);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return event;
    }

    public void serialized(File file)
    {
        boolean[] checkBox=new boolean[256];
        try {
            ObjectOutputStream outputStream=new ObjectOutputStream(new FileOutputStream(file));
            for (int i = 0; i < 256; i++) {
                if(checkBoxList.get(i).isSelected())
                    checkBox[i]=true;
                else
                    checkBox[i]=false;
            }
            outputStream.writeObject(checkBox);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deSerialized(File file)
    {
        boolean[] checkboxState=null;
        try {
            ObjectInputStream inputStream=new ObjectInputStream(new FileInputStream(file));
            checkboxState=(boolean[]) inputStream.readObject(); //the return type is Object and you should cast it.
            for (int i = 0; i < 256; i++) {
                if(checkboxState[i])
                    checkBoxList.get(i).setSelected(true);
                else
                    checkBoxList.get(i).setSelected(false);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        sequencer.stop();
        buildTrackAndStart();
    }

}
