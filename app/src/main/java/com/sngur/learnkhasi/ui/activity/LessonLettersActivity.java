package com.sngur.learnkhasi.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.ActivityLessonLettersBinding;

public class LessonLettersActivity extends AppCompatActivity {

    ActivityLessonLettersBinding binding;
    Bundle bundle;
    String lesson;
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_lesson_letters);
        binding=ActivityLessonLettersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bundle=getIntent().getExtras();
        if(bundle.getString("lesson")!=null)
            lesson=bundle.getString("lesson");
        else
            lesson="viewLetters";
        setTitle("Khasi Letters");
        if(lesson.equalsIgnoreCase("viewLetters")){
            binding.viewLetters.setVisibility(View.VISIBLE);
            binding.listenLetters.setVisibility(View.GONE);
        }
        else{
            binding.viewLetters.setVisibility(View.GONE);
            binding.listenLetters.setVisibility(View.VISIBLE);
        }
        binding.listenSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp= null;
                playAudio();
            }
        });
    }
    public void playAudio()
    {
        try{
            mp=new MediaPlayer();

            if (mp != null) {
                mp.reset();
                mp.release();
            }
            String i=binding.displayLetter.getText().toString()+"";
            switch(i)
            {
                case "A a":
                    mp=  MediaPlayer.create(this, R.raw.lettera);
                    break;
                case "B b":
                    mp = MediaPlayer.create(this, R.raw.letterb);
                    break;
                case "K k":
                    mp = MediaPlayer.create(this, R.raw.letterk);
                    break;
                case "D d":
                    mp = MediaPlayer.create(this, R.raw.letterd);
                    break;
                case "E e":
                    mp = MediaPlayer.create(this, R.raw.lettere);
                    break;
                case "G g":
                    mp = MediaPlayer.create(this, R.raw.letterg);
                    break;
                case "Ng ng":
                    mp = MediaPlayer.create(this, R.raw.letterng);
                    break;
                case "H h":
                    mp = MediaPlayer.create(this, R.raw.letterh);
                    break;
                case "I i":
                    mp = MediaPlayer.create(this, R.raw.letteri);
                    break;
                case "Ï ï":
                    mp = MediaPlayer.create(this, R.raw.letterii);
                    break;
                case "J j":
                    mp = MediaPlayer.create(this, R.raw.letterj);
                    break;
                case "L l":
                    mp = MediaPlayer.create(this, R.raw.letterl);
                    break;
                case "M m":
                    mp = MediaPlayer.create(this, R.raw.letterm);
                    break;
                case "N n":
                    mp = MediaPlayer.create(this, R.raw.lettern);
                    break;
                case "Ñ ñ":
                    mp = MediaPlayer.create(this, R.raw.letternn);
                    break;
                case "O o":
                    mp = MediaPlayer.create(this, R.raw.lettero);
                    break;
                case "P p":
                    mp = MediaPlayer.create(this, R.raw.letterp);
                    break;
                case "R r":
                    mp = MediaPlayer.create(this, R.raw.letterr);
                    break;
                case "S s":
                    mp = MediaPlayer.create(this, R.raw.letters);
                    break;
                case "T t":
                    mp = MediaPlayer.create(this, R.raw.lettert);
                    break;
                case "U u":
                    mp = MediaPlayer.create(this, R.raw.letteru);
                    break;
                case "W w":
                    mp = MediaPlayer.create(this, R.raw.letterw);
                    break;
                case "Y y":
                    mp = MediaPlayer.create(this, R.raw.lettery);
                    break;
            }

            mp.start();
        }catch (Exception e)
        {
            Toast.makeText(this, "Unable to play sound!", Toast.LENGTH_SHORT).show();
            //e.printStackTrace();
        }
    }

    public void setLetter(View view) {
        int i=view.getId();
        switch(i)
        {
            case R.id.letterA:
                binding.displayLetter.setText(binding.letterA.getText());
                break;
            case R.id.letterB:
                binding.displayLetter.setText(binding.letterB.getText());
                break;
            case R.id.letterK:
                binding.displayLetter.setText(binding.letterK.getText());
                break;
            case R.id.letterD:
                binding.displayLetter.setText(binding.letterD.getText());
                break;
            case R.id.letterE:
                binding.displayLetter.setText(binding.letterE.getText());
                break;
            case R.id.letterG:
                binding.displayLetter.setText(binding.letterG.getText());
                break;
            case R.id.letterNg:
                binding.displayLetter.setText(binding.letterNg.getText());
                break;
            case R.id.letterH:
                binding.displayLetter.setText(binding.letterH.getText());
                break;
            case R.id.letterI:
                binding.displayLetter.setText(binding.letterI.getText());
                break;
            case R.id.letterII:
                binding.displayLetter.setText(binding.letterII.getText());
                break;
            case R.id.letterJ:
                binding.displayLetter.setText(binding.letterJ.getText());
                break;
            case R.id.letterL:
                binding.displayLetter.setText(binding.letterL.getText());
                break;
            case R.id.letterM:
                binding.displayLetter.setText(binding.letterM.getText());
                break;
            case R.id.letterN:
                binding.displayLetter.setText(binding.letterN.getText());
                break;
            case R.id.letterNN:
                binding.displayLetter.setText(binding.letterNN.getText());
                break;
            case R.id.letterO:
                binding.displayLetter.setText(binding.letterO.getText());
                break;
            case R.id.letterP:
                binding.displayLetter.setText(binding.letterP.getText());
                break;
            case R.id.letterR:
                binding.displayLetter.setText(binding.letterR.getText());
                break;
            case R.id.letterS:
                binding.displayLetter.setText(binding.letterS.getText());
                break;
            case R.id.letterT:
                binding.displayLetter.setText(binding.letterT.getText());
                break;
            case R.id.letterU:
                binding.displayLetter.setText(binding.letterU.getText());
                break;
            case R.id.letterW:
                binding.displayLetter.setText(binding.letterW.getText());
                break;
            case R.id.letterY:
                binding.displayLetter.setText(binding.letterY.getText());
                break;
        }
    }
}