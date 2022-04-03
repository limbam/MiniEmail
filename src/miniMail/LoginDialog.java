package miniMail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LoginDialog extends JDialog {
    //  für den Benutzernamen und das Kennwort
    private static String loginName, kennWort;
    //  Eingabefelder für den Dialog
    JTextField iDFeld = new JTextField(15);
    JPasswordField pWFeld = new JPasswordField(15);

    //	die innere Klasse für den ActionListener
    class MeinListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
//			wurde auf Speichern geklickt?
            if (e.getActionCommand().equals("Speichern"))
//				dann speichern
                loginSchreiben();
//			wurde auf Abbrechen geklickt?
            if (e.getActionCommand().equals("Abbrechen"))
                abbruch();
        }
    }

    // der Kontruktor
    public LoginDialog(Frame titel) {
        super(titel, "Ihre Zugangsdaten");
        setModal(true);
//		wir nehmen ein Gridlayout
        getContentPane().setLayout(new GridLayout(4, 2));
//      lable zur textanzeige
        getContentPane().add(new JLabel("Bitte geben Sie ihre Zugangsdaten ein"));
//      leeres label
        getContentPane().add(new JLabel());
//		erstes Eingabefeld
        getContentPane().add(new JLabel("E-Mail:"));
        getContentPane().add(iDFeld);
//		zweites Eingabefeld
        getContentPane().add(new JLabel("Kennwort:"));
        getContentPane().add(pWFeld);

//		eine Schaltfläche zum speichern
        JButton ok = new JButton("Speichern");
        getContentPane().add(ok);
//		und eine zum abbrechen
        JButton cancel = new JButton("Abbrechen");
        getContentPane().add(cancel);

//		mit dem Actionlistener verbinden
        MeinListener listener = new MeinListener();
        cancel.addActionListener(listener);
        ok.addActionListener(listener);

//		packen und anzeigen
        pack();
        setVisible(true);
    }

    //die Methode speichert die eingegebenen daten in die Datei login.bin
    private void loginSchreiben() {
        try {
            FileWriter datei = new FileWriter("login.bin");
//			Benutzernamen in Datei schreiben
            datei.write(iDFeld.getText() + " ");
//			Kennwort in Datei schreiben
            datei.write(pWFeld.getPassword());
            datei.close();
//			den Dialog schließen
            setVisible(false);
//			falls etwas nicht geklappt hat
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Beim schreiben ist ein Problem aufgetreten");
        }
    }

    //die Methode beschafft den gespeicherten Benutzernamen aus der Datei login.bin
    public static String getLoginName() {
        try {
//			datei öffnen
            FileReader datei = new FileReader("login.bin");
//		    Zeile einlesen und zwischenspeichern
            BufferedReader br = new BufferedReader(datei);
            String zeile;
//			die gespeicherte Zeile splitten
            while ((zeile = br.readLine()) != null) {
                String[] daten = zeile.split(" ");
//				und den ersten Teil als Benutzernamen speichern
                loginName = daten[0];
            }
//			Datei wieder schließen
            datei.close();
//		falls etwas nicht geklappt hat
        } catch (Exception e) {

        }
//		Benutzernamen zurückliefern
        return loginName;
    }

    //	die Methode beschafft das gespeicherte Kennwort aus der Datei login.bin
    public static String getKennWort() {
        try {
//			datei öffnen
            FileReader datei = new FileReader("login.bin");
//		    Zeile einlesen und zwischenspeichern
            BufferedReader br = new BufferedReader(datei);
            String zeile;
//			die gespeicherte Zeile splitten
            while ((zeile = br.readLine()) != null) {
                String[] daten = zeile.split(" ");
//				und den zweiten teil als Kennwort speichern
                kennWort = daten[1];
            }
//			Datei wieder schließen
            datei.close();
//		falls etwas nicht geklappt hat
        } catch (Exception e) {

        }
//		Kennwort zurückliefern
        return kennWort;
    }

    //die Methode kümmert sich um den abbrechen buttton
    private void abbruch() {
        JOptionPane.showMessageDialog(this,
                "Sie haben keine Daten eingegeben,\nDas Programm wird nun wieder geschlossen.");
        System.exit(0);
    }
}


