package bomber.audio;

import bomber.game.AudioEvent;
import bomber.game.Constants;

import javax.sound.sampled.FloatControl;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexandru Rosu on 05.02.2017.
 */
public class AudioManager
{

    private static float musicVolume;
    private static float effectsVolume;

    private MusicPlayer music;
    private SoundEffectPlayer effects;

    public AudioManager()
    {
        music = new MusicPlayer(musicVolume);
        effects = new SoundEffectPlayer(effectsVolume);
        effects.start();
    }

    public static void setMusicVolume(float percent)
    {
        musicVolume = percent;
    }

    public static void setEffectsVolume(float percent)
    {
        System.err.println("Effects volume set to " + percent);
        effectsVolume = percent;
    }

    public static void setVolume(float percent)
    {
        musicVolume = percent;
        effectsVolume = percent;
    }

    static void setControlVolume(FloatControl gainControl, float volume)
    {

        if (volume>100 || volume<0)
        {
            System.err.println("Incorrect call: setVolume(" + volume + ")");
            return;
        }

        float linearMin = (float)Math.pow(10,gainControl.getMinimum()/20);
        float linearMax = (float)Math.pow(10,gainControl.getMaximum()/20);
        float linearVolume = linearMin + (linearMax - linearMin) * volume / 100;

        //System.out.println(linearMin + " -- " + linearVolume + " -- " + linearMax);
        //System.out.println(gainControl.getMinimum() + " -- " + (float)(20*Math.log10(linearVolume)) + " -- " + gainControl.getMaximum());

        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();
        //gainControl.setValue(min + (max-min)*volume/100); // db; decreases faster
        gainControl.setValue((float)(20*Math.log10(linearVolume))); // linear formula; not so precise with little volume
    }

    public void playMusic()
    {
        if (!music.isAlive())
            music.start();
    }

    public void pauseMusic()
    {
        music.pause();
    }

    public void unpauseMusic()
    {
        music.unpause();
    }

    public void stopAudio()
    {
        music.pause();
        music.interrupt();
        effects.interrupt();
    }

    public void playEventList(List<AudioEvent> eventList)
    {
        eventList.forEach(effects::play);
        eventList.clear();
    }

    public static void playMenuItemSelected()
    {
        SoundEffectPlayer effects = new SoundEffectPlayer(effectsVolume);
        effects.playSound(Constants.menuSoundFilename);
        effects.interrupt();
    }

    public static void playGameOverWon()
    {
        SoundEffectPlayer effects = new SoundEffectPlayer(effectsVolume);
        effects.playSound(Constants.gameOverWonFilename);
        effects.interrupt();
    }

    public static void playGameOverLost()
    {
        SoundEffectPlayer effects = new SoundEffectPlayer(effectsVolume);
        effects.playSound(Constants.gameOverLostFilename);
        effects.interrupt();
    }

    public static void main(String[] args) throws InterruptedException
    {
        AudioManager audioManager = new AudioManager();
        //audioManager.playMusic();
        audioManager.setVolume(100);

        //AudioManager.playMenuItemSelected();


        AudioManager.playGameOverWon();

        TimeUnit.SECONDS.sleep(3);

        AudioManager.playGameOverLost();

        TimeUnit.SECONDS.sleep(3);
        /*


        Scanner sc = new Scanner(System.in);

        while(true)
            audioManager.setVolume(sc.nextInt());

        /*
        for (int i = 100000; i >= 0; i--)
        {
            try
            {
                audioManager.setVolume(2);
                System.out.println(i);
                TimeUnit.MILLISECONDS.sleep(10);

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
/*

        try
        {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        audioManager.setVolume(50);
        System.out.println(50);
        try
        {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        audioManager.setVolume(25);
        System.out.println(25);

        try
        {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

*/


/*
        List<AudioEvent> eventList = new ArrayList<>();
        eventList.add(AudioEvent.EXPLOSION);

        try
        {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }


        audioManager.setVolume(-80f);
        //audioManager.playEventList(eventList);

        try
        {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        //audioManager.pauseMusic();
        //audioManager.setVolume(0f);

        try
        {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        //audioManager.setVolume(0f);
        //audioManager.unpauseMusic();

        try
        {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
*/
    }
}