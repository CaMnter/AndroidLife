package com.camnter.newlife.ui.activity.agera;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.google.android.agera.Function;
import com.google.android.agera.Functions;
import com.google.android.agera.Merger;
import com.google.android.agera.Observable;
import com.google.android.agera.Predicate;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Reservoirs;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Description：AgeraSimpleActivity
 * Created by：CaMnter
 * Time：2016-05-30 16:21
 */
public class AgeraSimpleActivity extends BaseAppCompatActivity {

    /***********************
     * Test - 8 - Function *
     ***********************/

    private static final String filterString = "???";
    @Bind(R.id.agera_observable_text_one) TextView observableOneText;
    @Bind(R.id.agera_observable_text_two) TextView observableTwoText;
    @Bind(R.id.agera_observable_text_three) TextView observableThreeText;
    @Bind(R.id.agera_observable_text_four) TextView observableFourText;
    @Bind(R.id.agera_observable_text_five) TextView observableFiveText;
    @Bind(R.id.agera_observable_text_six) TextView observableSixText;
    @Bind(R.id.agera_observable_text_seven) TextView observableSevenText;
    @Bind(R.id.agera_observable_text_eight) TextView observableEightText;
    /*************************
     * Test - 1 - Observable *
     *************************/

    private Observable observableTestOne = new Observable() {
        @Override public void addUpdatable(@NonNull Updatable updatable) {
            updatable.update();
        }


        @Override public void removeUpdatable(@NonNull Updatable updatable) {

        }
    };
    private Updatable updatableOne = new Updatable() {
        @Override public void update() {
            observableOneText.setText("Jud: " + UUID.randomUUID().toString());
        }
    };
    /*************************
     * Test - 2 - Repository *
     *************************/

    private Supplier<String> supplierTestTwo = new Supplier<String>() {
        @NonNull @Override public String get() {
            return "Jud: " + UUID.randomUUID().toString();
        }
    };
    private Repository<String> repositoryTestTwo = Repositories
        .repositoryWithInitialValue("Tes")
        .observe()
        .onUpdatesPerLoop()
        .thenGetFrom(this.supplierTestTwo)
        .compile();
    private Updatable updatableTwo = new Updatable() {
        @Override public void update() {
            observableTwoText.setText("Jud: " + UUID.randomUUID().toString());
        }
    };
    /************************
     * Test - 3 - Transform *
     ************************/

    private Repository<String> repositoryTestThree = Repositories
        .repositoryWithInitialValue("Tes")
        .observe()
        .onUpdatesPerLoop()
        .getFrom(new Supplier<Integer>() {
            @NonNull @Override public Integer get() {
                return 6;
            }
        })
        .transform(new Function<Integer, String>() {
            @NonNull @Override public String apply(@NonNull Integer input) {
                return "Save you from anything " + input;
            }
        })
        .thenMergeIn(new Supplier<Integer>() {
            @NonNull @Override public Integer get() {
                return 7;
            }
        }, new Merger<String, Integer, String>() {
            @NonNull @Override
            public String merge(@NonNull String s, @NonNull Integer integer) {
                return s + " and " + integer;
            }
        })
        .compile();
    private Updatable updatableThree = new Updatable() {
        @Override public void update() {
            observableThreeText.setText(repositoryTestThree.get());
        }
    };
    /***********************
     * Test - 4 - Executor *
     ***********************/

    private Executor executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
    private Repository<String> repositoryTestFour = Repositories
        .repositoryWithInitialValue("Tes")
        .observe()
        .onUpdatesPerLoop()
        .goTo(executor)
        .thenGetFrom(new Supplier<String>() {
            @NonNull @Override public String get() {
                if (Looper.myLooper() != null || Looper.myLooper() == Looper.getMainLooper()) {
                    return "Main UI Thread: Save you from anything";
                } else {
                    return "Child Thread(" + Thread.currentThread().getId() +
                        "): Save you from anything";
                }
            }
        })
        .compile();
    private Updatable updatableFour = new Updatable() {
        @Override public void update() {
            observableFourText.setText(repositoryTestFour.get());
        }
    };
    /*****************************
     * Test - 5 - Error Handling *
     *****************************/

    private Repository<String> repositoryTestFive = Repositories
        .repositoryWithInitialValue("Tes")
        .observe()
        .onUpdatesPerLoop()
        .attemptGetFrom(new Supplier<Result<String>>() {
            @NonNull @Override public Result<String> get() {
                try {
                    throw new RuntimeException("Save you from anything 06");
                } catch (Exception e) {
                    return Result.failure(e);
                }
            }
        })
        .orEnd(new Function<Throwable, String>() {
            @NonNull @Override public String apply(@NonNull Throwable input) {
                return "Throwable message: " + input.getMessage();
            }
        })
        .thenTransform(new Function<String, String>() {
            @NonNull @Override public String apply(@NonNull String input) {
                return input;
            }
        })
        .compile();
    private Updatable updatableFive = new Updatable() {
        @Override public void update() {
            observableFiveText.setText(repositoryTestFive.get());
        }
    };
    /***********************
     * Test - 6 - Receiver *
     ***********************/

    private Repository<Result<String>> repositoryTestSix = Repositories
        .repositoryWithInitialValue(Result.<String>absent())
        .observe()
        .onUpdatesPerLoop()
        .attemptGetFrom(new Supplier<Result<String>>() {
            @NonNull @Override public Result<String> get() {
                try {
                    throw new RuntimeException("Save you from anything 06");
                } catch (Exception e) {
                    return Result.failure(e);
                }
            }
        })
        .orEnd(new Function<Throwable, Result<String>>() {
            @NonNull @Override public Result<String> apply(@NonNull Throwable input) {
                return Result.failure(input);
            }
        })
        .thenTransform(new Function<String, Result<String>>() {
            @NonNull @Override public Result<String> apply(@NonNull String input) {
                return Result.absentIfNull(input);
            }
        })
        .compile();
    private Updatable updatableSix = new Updatable() {
        @Override public void update() {
            repositoryTestSix
                .get()
                .ifFailedSendTo(new Receiver<Throwable>() {
                    @Override public void accept(@NonNull Throwable value) {
                        observableSixText.setText("ifFailedSendTo -> " + value);
                    }
                })
                .ifSucceededSendTo(new Receiver<String>() {
                    @Override public void accept(@NonNull String value) {
                        observableSixText.setText("ifSucceededSendTo -> " + value);
                    }
                });
        }
    };
    /************************
     * Test - 7 - Reservoir *
     ************************/

    private Reservoir<String> reservoir = Reservoirs.reservoir();
    private Repository<String> repositoryTestSeven = Repositories
        .repositoryWithInitialValue("Tes")
        .observe()
        .onUpdatesPerLoop()
        .attemptGetFrom(reservoir)
        .orSkip()
        .thenTransform(new Function<String, String>() {
            @NonNull @Override public String apply(@NonNull String input) {
                return "Reservoir test: Save you from anything " + input;
            }
        })
        .compile();
    private Updatable updatableSeven = new Updatable() {
        @Override public void update() {
            observableSevenText.setText(repositoryTestSeven.get());
        }
    };
    private Function<String, Integer> function = Functions
        .functionFrom(String.class)
        .unpack(
            new Function<String, List<String>>() {
                @NonNull @Override public List<String> apply(@NonNull String input) {
                    List<String> list = new ArrayList<>();
                    list.add("Function test:");
                    list.add(" ");
                    list.add(input);
                    list.add(" ");
                    list.add("you");
                    list.add(" ");
                    list.add("from");
                    list.add(" ");
                    list.add("anything");
                    list.add(" ");
                    list.add(filterString);
                    return list;
                }
            })
        .filter(new Predicate<String>() {
            @Override public boolean apply(@NonNull String value) {
                return value.equals(filterString);
            }
        })
        .map(new Function<String, byte[]>() {
            @NonNull @Override public byte[] apply(@NonNull String input) {
                return input.getBytes();
            }
        })
        .thenApply(new Function<List<byte[]>, Integer>() {
            @NonNull @Override public Integer apply(@NonNull List<byte[]> input) {
                int totalLength = 0;
                for (byte[] byteArray : input) {
                    totalLength += byteArray.length;
                }
                return totalLength;
            }
        });

    private Repository<String> repositoryTestEight = Repositories
        .repositoryWithInitialValue("Tes")
        .observe()
        .onUpdatesPerLoop()
        .getFrom(
            new Supplier<String>() {
                @NonNull @Override public String get() {
                    return "Save";
                }
            })
        .transform(function)
        .thenTransform(new Function<Integer, String>() {
            @NonNull @Override public String apply(@NonNull Integer input) {
                return "Function test: list size = " + input;
            }
        })
        .compile();

    private Updatable updatableEight = new Updatable() {
        @Override public void update() {
            observableEightText.setText(repositoryTestEight.get());
        }
    };


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_agera_simple;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.setTitle("AgeraSimpleActivity");
        ButterKnife.bind(this);
        this.playAgera();
    }


    private void playAgera() {
        this.observableTestOne.addUpdatable(this.updatableOne);
        this.repositoryTestTwo.addUpdatable(this.updatableTwo);
        this.repositoryTestThree.addUpdatable(this.updatableThree);
        this.repositoryTestFour.addUpdatable(this.updatableFour);
        this.repositoryTestFive.addUpdatable(this.updatableFive);
        this.repositoryTestSix.addUpdatable(this.updatableSix);
        // Reservoir
        this.repositoryTestSeven.addUpdatable(this.updatableSeven);
        this.reservoir.accept("CaMnter");
        //this.repositoryTestEight.addUpdatable(this.updatableEight);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }
}
