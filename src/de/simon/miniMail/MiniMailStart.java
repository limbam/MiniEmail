package de.simon.miniMail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MiniMailStart extends JFrame {
    //	die innere Klasse für den ActionListener
    class MeinListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
//			wurde auf Senden geklickt?
            if (e.getActionCommand().equals("senden"))
//				dann das Senden starten
                senden();
//			wurde auf Empfangen geklickt?
            if (e.getActionCommand().equals("empfangen"))
//				dann das Empfangen starten
                empfangen();
//			wurde auf Beenden geklickt?
            if (e.getActionCommand().equals("ende"))
//				dann beenden
                beenden();
        }
    }

    //	der Konstruktor
    public MiniMailStart(String titel) {
        super(titel);
//		ein FlowLayout
        setLayout(new FlowLayout(FlowLayout.LEFT));

//		die Schaltflächen
        JButton liste = new JButton("Senden");
        liste.setActionCommand("senden");
        JButton einzel = new JButton("Empfangen");
        einzel.setActionCommand("empfangen");
        JButton beenden = new JButton("Beenden");
        beenden.setActionCommand("ende");
//		Listener verbinden
        MeinListener listener = new MeinListener();
        liste.addActionListener(listener);
        einzel.addActionListener(listener);
        beenden.addActionListener(listener);
//		Schaltflächen hinzufügen
        add(liste);
        add(einzel);
        add(beenden);

//		Größe setzen, Standardverhalten festlegen und anzeigen
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

//		wenn keine Login informationen gespeichert sind...Öffnen wir den Dialog
        if (LoginDialog.getLoginName() == null && LoginDialog.getKennWort() == null) {
            new LoginDialog(null);
//			sonst...nicht
        } else {
            return;
        }
    }

    //	die Methoden
    private void senden() {
        new Senden();
    }

    private void empfangen() {
        new Empfangen();
    }

    private void beenden() {
        System.exit(0);
    }
}
