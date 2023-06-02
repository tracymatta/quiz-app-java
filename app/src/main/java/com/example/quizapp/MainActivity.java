package com.example.quizapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    AssetManager assets;
    String[] images;
    ImageView img;
    TextView resultText;
    TextView header;
    TextView optionsChoice;
    TextView regionsChoice;
    ImageButton nextQuestion;
    LinearLayout buttonLayout;
    LinearLayout landscapeLayout;
    int counter, score;
    String rightAnswer;
    ArrayList<Integer> doneImages;
    ArrayList<Button> buttons;
    ArrayList<Integer> options;
    ArrayList<String> continents;
    boolean[] selected = new boolean[] {true, true, true, true, true};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counter = 1;
        score = 0;
        doneImages = new ArrayList<Integer>(10);
        buttons = new ArrayList<Button>(9);
        options = new ArrayList<Integer>(9);
        continents = new ArrayList<String>(5);
        img = (ImageView) findViewById(R.id.flag);
        header = (TextView) findViewById(R.id.header);
        nextQuestion = (ImageButton) findViewById(R.id.imageButton);
        resultText = (TextView) findViewById(R.id.answer);
        buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);
        landscapeLayout = (LinearLayout) findViewById(R.id.landscape);
        optionsChoice = (TextView) findViewById(R.id.options);
        regionsChoice = (TextView) findViewById(R.id.regions);
        continents.add("Africa");
        continents.add("America");
        continents.add("Asia");
        continents.add("Europe");
        continents.add("Oceania");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        MainActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        if(width > height) {
            landscapeLayout.setVisibility(View.VISIBLE);
        }
        else {
            landscapeLayout.setVisibility(View.GONE);
        }

        regionsChoice.setText("Regions : " + String.join(" ", continents));

        createOptions();

        assets = this.getAssets(); //get reference on assets folder
        try {
            //the array images contains the names of all files inside the folder assets/png
            images = assets.list("png");
            generateQuestion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_xml, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_options:
                chooseOptions();
                return true;
            case R.id.choose_continents:
                chooseContinents();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseOptions() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Choose how many options you would like to have:");
        final CharSequence[] choiceList = {"3", "6", "9"};

        alertDialogBuilder.setSingleChoiceItems(
            choiceList,
            -1, // does not select anything
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int index) {
                    optionsChoice.setText("Number of choices : " + (3 * (index + 1)));
                    switch (index) {
                        case 0:
                            setOptions(3);
                            break;
                        case 1:
                            setOptions(6);
                            break;
                        case 2:
                            setOptions(9);
                            break;
                        default:
                            break;
                    }
                    dialog.dismiss();
                }
            }
        );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void createOptions() {
        Button b;
        LinearLayout row;
        for(int i = 0; i < 3; i += 1) {
            row = new LinearLayout(MainActivity.this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            for(int j = 0; j < 3; j += 1) {
                b = new Button(MainActivity.this);
                b.setId(j + 1 + (i * 3));
                b.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
                );
                if(i > 0) b.setVisibility(View.GONE);
                else buttons.add(b);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkAnswer(v);
                    }
                });
                row.addView(b);
            }
            buttonLayout.addView(row);
        }
        optionsChoice.setText("Number of choices : 3");
    }

    private void setOptions(int numOfOptions) {
        buttons.clear();
        Button b;
        for(int i = 1; i < numOfOptions + 1; i += 1) {
            b = (Button) findViewById(i);
            b.setVisibility(View.VISIBLE);
            buttons.add(b);
        }

        for(int i = numOfOptions + 1; i < 10; i += 1) {
            b = (Button) findViewById(i);
            b.setVisibility(View.GONE);
        }

        counter = 1;
        score = 0;
        generateQuestion();
    }

    private void chooseContinents() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Choose the continents you would like to be quizzed from:");
        final String[] continentsList = new String[]{"Africa", "America", "Asia", "Europe", "Oceania"};
        boolean[] currentSelected = (boolean[]) selected.clone();
        ArrayList<String> choices = new ArrayList<String>();
        for(int i = 0; i < continents.size(); i += 1) {
            choices.add(continents.get(i));
        }

        alertDialogBuilder.setMultiChoiceItems(
            continentsList,
            currentSelected, // does not select anything
            new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if(isChecked) {
                        currentSelected[which] = true;
                        choices.add(continentsList[which]);
                    }
                    else {
                        currentSelected[which] = false;
                        choices.remove(continentsList[which]);
                    }
                }
            }
        );

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(choices.size() == 0) {
                    Toast.makeText(MainActivity.this, "Error: cannot uncheck all continent choices!", Toast.LENGTH_LONG).show();
                }
                else {
                    continents = choices;
                    selected = currentSelected;
                    regionsChoice.setText("Regions : " + String.join(" ", continents));
                }
                counter = 1;
                score = 0;
                generateQuestion();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void generateQuestion() {
        header.setText("Question " + counter + " of 10");

        int randomInt;
        do {
            randomInt = (int) (Math.random() * 222);
        } while(doneImages.contains(randomInt));
        doneImages.add(randomInt);
        options.add(randomInt);

        String randomImage = images[randomInt];
        rightAnswer = randomImage.substring(randomImage.indexOf("-") + 1, randomImage.indexOf("."));

        try {
            img.setImageDrawable(Drawable.createFromStream(assets.open("png/" + randomImage), null));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Collections.shuffle(buttons);

        buttons.get(0).setText(rightAnswer);

        int randomOption;
        String randomOptionName;

        for(int i = 1; i < buttons.size(); i += 1) {
            do {
                randomOption = (int) (Math.random() * 222);
                randomOptionName = images[randomOption];
            } while(options.contains(randomOption) || !continents.contains(randomOptionName.substring(0, randomOptionName.indexOf("-"))));
            options.add(randomOption);
            buttons.get(i).setText(randomOptionName.substring(randomOptionName.indexOf("-") + 1, randomOptionName.indexOf(".")));
        }

        options.clear();

        buttons.forEach(button -> button.setClickable(true));
        nextQuestion.setClickable(false);

        resultText.setText("");

        counter += 1;
    }

    public void checkAnswer(View v) {
        String answerGiven = ((Button) v).getText().toString();

        if(answerGiven.equalsIgnoreCase(rightAnswer)) {
            score += 1;
        }
        resultText.setText(rightAnswer + "!");

        buttons.forEach(button -> button.setClickable(false));
        nextQuestion.setClickable(true);
    }

    public void nextQuestion(View v) {
        if(counter > 10) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setMessage((10 - score) + " wrong clicks, " + (score * 10) + "% correct");
            alertDialogBuilder.setNeutralButton("Reset", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                    counter = 1;
                    score = 0;
                    generateQuestion();
               }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else {
            generateQuestion();
        }
    }
}