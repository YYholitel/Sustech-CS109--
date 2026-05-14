package app;

import ui.InitFrame;
import ui.UiFont;
import utils.RandomMusicPlayer;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        RandomMusicPlayer musicPlayer = RandomMusicPlayer.fromDefaultMusicDir();
        musicPlayer.startLoop();
        Runtime.getRuntime().addShutdownHook(new Thread(musicPlayer::stop, "music-player-shutdown"));

        UiFont.installDefaults();
        SwingUtilities.invokeLater(InitFrame::new);
    }
}
