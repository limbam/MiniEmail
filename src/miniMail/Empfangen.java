package miniMail;

import javax.mail.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class Empfangen extends JFrame {
    //	für die Tabelle
    private JTable tabelle;
    //	für das Modell
    private DefaultTableModel modell;

    //	die Variablen
    static String absender;
    static String reBetreff;
    static String reInhalt;

    //	für die Aktionen
    private MeineAktionen antwortenAct, weiterleitenAct;

    // eine innere Klasse für die Aktionen
    public class MeineAktionen extends AbstractAction {
        // automatisch über Eclipse ergänzt
        private static final long serialVersionUID = 8673560298548765044L;

        // der Konstruktor
        public MeineAktionen(String text, ImageIcon icon, String beschreibung, KeyStroke shortcut, String actionText) {
            // den Konstruktor der übergeordneten Klasse mit dem Text und dem Icon aufrufen
            super(text, icon);
            // die Beschreibung setzen für den Bildschirmtipp
            putValue(SHORT_DESCRIPTION, beschreibung);
            // den Shortcut...wird nicht verwendet
            putValue(ACCELERATOR_KEY, shortcut);
            // das ActionCommand
            putValue(ACTION_COMMAND_KEY, actionText);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//			wurde auf Antworten geklickt?
            if (e.getActionCommand().equals("antworten")) {
//				wurde eine Nachricht ausgewählt?
                if (tabelle.getSelectedRow() != -1) {
//				dann die benötigten Inhalte über die getter Methoden beschaffen
                    getAbsender();
                    getBetreff();
                    getInhalt();
//				und die Antworten klasse ausführen
                    new Antworten(null, true);
//					sonst eine Meldung ausgeben
                } else {
                    JOptionPane.showMessageDialog(Empfangen.this, "Bitte wählen Sie die gewünschte Nachricht aus");
                }
            }
//			wurde auf Weiterleiten geklickt?
            if (e.getActionCommand().equals("weiterleiten")) {
//				wurde eine Nachricht ausgewählt?
                if (tabelle.getSelectedRow() != -1) {
//				dann die benötigten Inhalte über die getter Methoden beschaffen
                    getBetreff();
                    getInhalt();
//				und die Weiterleiten klasse ausführen
                    new Weiterleiten(null, true);
//				sonst eine Meldung ausgeben
                } else {
                    JOptionPane.showMessageDialog(Empfangen.this, "Bitte wählen Sie die gewünschte Nachricht aus");
                }
            }
        }
    }

    //	die Symbolleiste erzeugen und zurückgeben
    private JToolBar symbolleiste() {
        JToolBar leiste = new JToolBar();
//	 die Symbole über die Aktionen einbauen
        leiste.add(antwortenAct);
        leiste.add(weiterleitenAct);

//	die Leiste zurückgeben
        return (leiste);
    }

    //  eine innere Klasse für den WindowListener und den ActionListener
//	die Klasse ist von WindowAdapter abgeleitet
    class MeinWindowAdapter extends WindowAdapter {
        //		für das öffnen des Fensters
        @Override
        public void windowOpened(WindowEvent e) {
//			die Methode nachrichtenEmpfangen() aufrufen
            nachrichtenEmpfangen();
        }
    }

    //	der Konstruktor
    Empfangen() {
        super();

        setTitle("E-Mail empfangen");
//		wir nehmen ein Border-Layout
        setLayout(new BorderLayout());

        setVisible(true);
        setSize(700, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		die Aktionen erstellen
        antwortenAct = new MeineAktionen("Auf E-Mail antworten", new ImageIcon("icons/mail-reply.gif"),
                "Auf die ausgewählte E-Mail antworten", null, "antworten");

        weiterleitenAct = new MeineAktionen("E-Mail weiterleiten", new ImageIcon("icons/mail-forward.gif"),
                "Leitet die ausgewählte E-Mail weiter", null, "weiterleiten");
//		die Symbolleiste oben einfügen
        add(symbolleiste(), BorderLayout.NORTH);

//		den Listener verbinden
        addWindowListener(new MeinWindowAdapter());

//		die Tabelle erstellen und anzeigen
        tabelleErstellen();
        tabelleAktualisieren();
    }

    //	zum Erstellen der Tabelle
    private void tabelleErstellen() {
//		für die Spaltenbezeichner
        String[] spaltenNamen = {"ID", "Sender", "Betreff", "Text"};

//		ein neues Standardmodell erstellen
        modell = new DefaultTableModel();
//		die Spaltennamen setzen
        modell.setColumnIdentifiers(spaltenNamen);
//		die Tabelle erzeugen
        tabelle = new JTable();
//		und mit dem Modell verbinden
        tabelle.setModel(modell);
//		wir haben keinen Editor, können die Tabelle also nicht bearbeiten
        tabelle.setDefaultEditor(Object.class, null);
//		es sollen immer alle Spalten angepasst werden
        tabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//		und die volle Größe genutzt werden
        tabelle.setFillsViewportHeight(true);
//		die Tabelle setzen wir in ein Scrollpane
        JScrollPane scroll = new JScrollPane(tabelle);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);

//		einen Maus-Listener ergänzen
        tabelle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
//				war es ein Doppelklick?
                if (e.getClickCount() == 2) {
//					die Zeile beschaffen
                    int zeile = tabelle.getSelectedRow();
//					die Daten beschaffen
                    String sender, betreff, inhalt, ID;
                    ID = tabelle.getModel().getValueAt(zeile, 0).toString();
                    sender = tabelle.getModel().getValueAt(zeile, 1).toString();
                    betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
                    inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();
//					und anzeigen
//					übergeben wird der Frame der äußeren Klasse
                    new Anzeige(Empfangen.this, true, ID, sender, betreff, inhalt);
                }
            }
        });
    }

    private void tabelleAktualisieren() {
//		für den Datenbankzugriff
        Connection verbindung;
        ResultSet ergebnisMenge;

//		für die Spalten
        String sender, betreff, inhalt, ID;
//		die Inhalte löschen
        modell.setRowCount(0);

        try {
//			Verbindung herstellen und Ergebnismenge beschaffen
            verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
            ergebnisMenge = MiniDBTools.liefereErgebnis(verbindung, "SELECT * FROM empfangen");
//			die Einträge in die Tabelle schreiben
            while (ergebnisMenge.next()) {
                ID = ergebnisMenge.getString("iNummer");
                sender = ergebnisMenge.getString("sender");
                betreff = ergebnisMenge.getString("betreff");
//				den Inhalt vom CLOB beschaffen und in einen String umbauen
                Clob clob;
                clob = ergebnisMenge.getClob("inhalt");
                inhalt = clob.getSubString(1, (int) clob.length());

//				die Zeile zum Modell hinzufügen
//				dazu benutzen wir ein Array vom Typ Object
                modell.addRow(new Object[]{ID, sender, betreff, inhalt});
            }
//			die Verbindungen wieder schließen und trennen
            ergebnisMenge.close();
            verbindung.close();
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtenEmpfangen() {
        nachrichtenAbholen();
//		nach dem Empfangen lassen wir die Anzeige aktualisieren
        tabelleAktualisieren();
    }

    private void nachrichtenAbholen() {
//		die Zugangsdaten aus der Klasse LoginDialog beschaffen
        String benutzername = LoginDialog.getLoginName();
        String kennwort = LoginDialog.getKennWort();

//		der Server
        String server = "pop.gmail.com";

//		die Eigenschaften setzen
        Properties eigenschaften = new Properties();
//		das Protokoll
        eigenschaften.put("mail.store.protocol", "pop3");
//		den Host
        eigenschaften.put("mail.pop3.host", server);
//		den Port zum Empfangen
        eigenschaften.put("mail.pop3.port", "995");
//		die Authentifizierung über TLS
        eigenschaften.put("mail.pop3.starttls.enable", "true");
//		das Session-Objekt erstellen
        Session sitzung = Session.getDefaultInstance(eigenschaften);

//		das Store-Objekt über die Sitzung erzeugen
        try (Store store = sitzung.getStore("pop3s")) {
//			und verbinden
            store.connect(server, benutzername, kennwort);
//			ein Ordnerobjekt für den Posteingang erzeugen
            Folder posteingang = store.getFolder("INBOX");
//			und öffnen
//			dabei sind auch änderungen zugelassen
            posteingang.open(Folder.READ_WRITE);

//			die Nachrichten beschaffen
            Message nachrichten[] = posteingang.getMessages();

//			gibt es neue Nachrichten?
            if (nachrichten.length != 0) {
//				dann die Anzahl zeigen
                JOptionPane.showMessageDialog(this,
                        "Es gibt " + posteingang.getUnreadMessageCount() + " neue Nachrichten.");
//				jede Nachricht verarbeiten
                for (Message nachricht : nachrichten)
                    nachrichtVerarbeiten(nachricht);
            } else
                JOptionPane.showMessageDialog(this, "Es gibt keine neue Nachrichten.");

//			den Ordner schließen
//			durch das Argument true werden die Nachrichten gelöscht
            posteingang.close(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtVerarbeiten(Message nachricht) {
        try {
//			ist es einfacher Text?
            if (nachricht.isMimeType("text/plain;")) {
//				den ersten Sender beschaffen
                String sender = nachricht.getFrom()[0].toString();
//				den Betreff
                String betreff = nachricht.getSubject();
//				den Inhalt
                String inhalt = nachricht.getContent().toString();
//				und die Nachricht speichern
                nachrichtSpeichern(sender, betreff, inhalt);
//				und zum Löschen markieren
                nachricht.setFlag(Flags.Flag.DELETED, true);
            }
//			sonst geben wir eine Meldung aus
            else
                JOptionPane.showMessageDialog(this,
                        "Der Typ der Nachricht " + nachricht.getContentType() + "kann nicht verarbeitet werden.");
        } catch (

                Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtSpeichern(String sender, String betreff, String inhalt) {
//		für die Verbindung
        Connection verbindung;

//		die Datenbank öffnen
        verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
        try {
//			einen Eintrag in der Tabelle empfangen anlegen
//			über ein vorbereitetes Statement
            PreparedStatement prepState;
            prepState = verbindung.prepareStatement("insert into empfangen (sender, betreff, inhalt) values (?, ?, ?)");
            prepState.setString(1, sender);
            prepState.setString(2, betreff);
            prepState.setString(3, inhalt);
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

    //die Methode liefert den Absender der ausgewählten Nachricht zurück
    String getAbsender() {
        int auswahl = tabelle.getSelectedRow();
        absender = tabelle.getModel().getValueAt(auswahl, 1).toString();
        return absender;
    }

    //die Methode liefert den Betreff der ausgewählten Nachricht zurück
    String getBetreff() {
        int auswahl = tabelle.getSelectedRow();
        reBetreff = tabelle.getModel().getValueAt(auswahl, 2).toString();
        return (reBetreff);
    }

    //die Methode liefert den Inhalt der ausgewählten Nachricht zurück
    String getInhalt() {
        int auswahl = tabelle.getSelectedRow();
        reInhalt = tabelle.getModel().getValueAt(auswahl, 3).toString();
        return (reInhalt);
    }
}
