package peertutoring;

import peertutoring.data.DataStore;
import peertutoring.ui.AppContext;
import peertutoring.ui.MainFrame;
import peertutoring.ui.Theme;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.Paths;


public class Main {

    public static void main(String[] args) {
        String dataFolder = args.length > 0 ? args[0] : "data";
        DataStore store = new DataStore(Paths.get(dataFolder));
        try {
            store.load();
        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "The data files could not be read:\n" + e.getMessage()
                            + "\n\nDelete the \"" + dataFolder + "\" folder to start again with an empty system.",
                    "Peer Tutoring System", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Theme.install();
        new MainFrame(new AppContext(store)).setVisible(true);
    }
}
