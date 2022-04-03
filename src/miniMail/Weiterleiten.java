package miniMail;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Weiterleiten extends JDialog {
    //	für die Eingabefelder
    private JTextField empfaenger, betreff;
    private JTextArea inhalt;
    //	für die Schaltflächen
    private JButton ok, abbrechen;

    //	die innere Klasse für den ActionListener
    class NeuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
//			wurde auf OK geklickt?
            if (e.getActionCommand().equals("senden"))
//				dann die Daten übernehmen
                senden();
//			wurde auf Abbrechen geklickt?
            if (e.getActionCommand().equals("abbrechen"))
//				dann Dialog schließen
                dispose();
        }
    }

    //	der Konstruktor
    public Weiterleiten(JFrame parent, boolean modal) {
        super(parent, modal);
        setTitle("Weiterleiten");
//		die Oberfläche erstellen
        initGui();

//		Standardoperation setzen
//		hier den Dialog ausblenden und löschen
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initGui() {
        setLayout(new BorderLayout());
        JPanel oben = new JPanel();
        oben.setLayout(new GridLayout(0, 2));
        oben.add(new JLabel("Empfänger:"));
        empfaenger = new JTextField();
        oben.add(empfaenger);
//		Betreff label
        oben.add(new JLabel("Betreff:"));
        betreff = new JTextField();
        oben.add(betreff);
//		den Betreff setzen und für weitere Eingaben Sperren
        betreff.setText("WG: " + Empfangen.reBetreff);
        betreff.setEditable(false);

//		Inhalt textarea
        add(oben, BorderLayout.NORTH);
        inhalt = new JTextArea();
//		den Zeilenumbruch aktivieren
        inhalt.setLineWrap(true);
        inhalt.setWrapStyleWord(true);

//		den Inhalt mit einer Kennzeichnung setzen
        inhalt.setText("\n----- Text der weitergeleiteten Nachricht ----\n" + Empfangen.reInhalt);

//		das Feld setzen wir in ein Scrollpane
        JScrollPane scroll = new JScrollPane(inhalt);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);
        JPanel unten = new JPanel();
//		die Schaltflächen
        ok = new JButton("Senden");
        ok.setActionCommand("senden");
        abbrechen = new JButton("Abbrechen");
        abbrechen.setActionCommand("abbrechen");

        NeuListener listener = new NeuListener();
        ok.addActionListener(listener);
        abbrechen.addActionListener(listener);

        unten.add(ok);
        unten.add(abbrechen);
        add(unten, BorderLayout.SOUTH);

//		anzeigen
        setSize(600, 300);
        setVisible(true);
    }

    //	die Methode verschickt die Nachricht
    private void senden() {
//		für die Sitzung
        Session sitzung;

        // die Verbindung herstellen
        sitzung = verbindungHerstellen();
        // die Nachricht verschicken und speichern
        nachrichtVerschicken(sitzung);
        nachrichtSpeichern();
    }

    private Session verbindungHerstellen() {
//		die Zugangsdaten aus der Klasse LoginDialog beschaffen
        String benutzername = LoginDialog.getLoginName();
        String kennwort = LoginDialog.getKennWort();

//		der Server
        String server = "smtp.gmail.com";

//		die Eigenschaften setzen
        Properties eigenschaften = new Properties();
//		die Authentifizierung über TLS
        eigenschaften.put("mail.smtp.auth", "true");
        eigenschaften.put("mail.smtp.starttls.enable", "true");
//		der Server
        eigenschaften.put("mail.smtp.host", server);
//		der Port zum Versenden
        eigenschaften.put("mail.smtp.port", "587");

//		das Session-Objekt erstellen
        Session sitzung = Session.getInstance(eigenschaften, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(benutzername, kennwort);
            }
        });

        return sitzung;
    }

    private void nachrichtVerschicken(Session sitzung) {
//		LoginName verwenden wir auch als "Absender"
        String absender = LoginDialog.getLoginName();

        try {
//			eine neue Nachricht vom Typ MimeMessage erzeugen
            MimeMessage nachricht = new MimeMessage(sitzung);
//			den Absender setzen
            nachricht.setFrom(new InternetAddress(absender));
//			den Empfänger
            nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfaenger.getText()));
//			den Betreff
            nachricht.setSubject(betreff.getText());
//			und den Text
            nachricht.setText(inhalt.getText());
//			die Nachricht verschicken
            Transport.send(nachricht);

            JOptionPane.showMessageDialog(this, "Die Nachricht wurde verschickt.");

//			den Dialog schließen
            dispose();

        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtSpeichern() {
//		für die Verbindung
        Connection verbindung;

//		die Datenbank öffnen
        verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
        try {
//			einen Eintrag in der Tabelle gesendet anlegen
//			über ein vorbereitetes Statement
            PreparedStatement prepState;
            prepState = verbindung
                    .prepareStatement("insert into gesendet (empfaenger, betreff, inhalt) values (?, ?, ?)");
            prepState.setString(1, empfaenger.getText());
            prepState.setString(2, betreff.getText());
            prepState.setString(3, inhalt.getText());
//			das Statement ausführen
            prepState.executeUpdate();
            verbindung.commit();

//			Verbindung schließen
            prepState.close();
            verbindung.close();
//			und die Datenbank schließen
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }
}

