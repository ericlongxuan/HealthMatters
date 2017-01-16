package edu.dartmouth.cs.healthmatters;

/**
 * Created by varun on 3/22/16.
 */
public class Globals {

    public static String UPLOAD_URL = "https://biorhythm.cs.dartmouth.edu/upload";

    public static String LOGIN_PREFERENCES = "LoginPrefs";
    public static String USERNAME_KEY = "usernameKey";
    public static String PASSWORD_KEY = "passwordKey";

    public static String POPUP_PREFERENCES = "PopupPrefs";
    public static String POPUP_TIMESTAMP = "popuptimeKey";
    public static String POPUP_QUESTIONS = "popupquesKey";
    public static String POPUP_INTENT_EXTRA = "popupId";

    public static String POLL_PREFERENCES = "PollPrefs";
    public static String POLL_TIMESTAMP = "polltimeKey";
    public static String POLL_QUESTIONS = "pollquesKey";
    public static String POLL_INTENT_EXTRA = "pollId";

    public static String SERVICE_PREFERENCE = "ServicePref";
    public static String SERIVCE_PREF_START = "servicestartKey";


    public static String POLL_RESPONSE_INTENT_EXTRA = "pollResponse";


    public static long TIME_GAP = 2*1000*3600; //hours
    public static long POLL_TIME_GAP = 2*1000*3600; //hours

    public static String[] SELF_AFFIRM = {"Change your Attitude to Gratitude.  What are 5 things you are grateful for today?",
            "Think about 3 people or things that make you truly happy.",
            "Do your little bit of good where you are. \n" +
                    "It's those little bits of good put together that overwhelm the world. \n" +
                    "\n" +
                    "Desmond Tutu - South African social rights activist and former Archbishop",
            "We do not need magic to change the world.\n" +
                    "We carry all the power we need inside ourselves already; \n" +
                    "we have the power to imagine better. \n" +
                    "\n" +
                    "JK Rowling, author",
            "Focus on breathing and grounding yourself. Breathe in calmness and breathe out nervousness. Trust your inner wisdom and intuition. Trust yourself.",
            "Practice random acts of kindness. It’s good for your health!",
            "Done is better than perfect. \n" +
                    "\n" +
                    "Sheryl Sandberg - Chief Operating Officer, Facebook",
            "Mindfulness = \n" +
                    "Paying attention on purpose, in the present moment, and nonjudgmentally \n" +
                    "\n" +
                    "Jon Kabat-Zinn",
            "Stop for a moment. Look around. \n" +
                    "What are thoughts, feelings, sounds, and smells in this moment?",
            "Be as compassionate with yourself as you would with a puppy. When you notice that your mind has wandered, gently and kindly guide it back to the present moment.",
            " Lost in thoughts or worries? \n" +
                    "Stop and take a deep breath, and bring your attention to this moment. \n" +
                    "This will give your brain a break.",
            "Simplify. You can do anything, but not everything.",
            "Think deeply about habits that guide your daily actions. \n" +
                    "Have the courage to lose sight of old habits to make room for new ones to begin!",
            "Hydrate! Drinking >10 glasses of water a day helps combat fatigue.",
            "Sleep is when brains and bodies rebuild and repair. Tune into your bedtime and waketime each day.",
            "Feeling depleted? \n" +
                    "Try recharging with natural alternatives: work out, space out meals, walk outside, hydrate, get sleep, stretch, meditate.\n"};


    public static String[] SELF_AFFIRM_INDEX = {"0", "1", "2","3","4","5","6","7","8","9","10","11","12","13","14","15"};

    public static String[] POLL_QUESTIONS_LIST = {"How much sleep did you get last night? \nResponse scale: from 0  to 10+ hours \n",
            "How stressed do you feel now? Rate your stress.  \n" +
                "Response scale:  0 ~ 10\n",
            "How happy did you feel yesterday?\n" +
                "Not at all = 1,  Very happy = 10",
            "How well have your nourished yourself today?\n" +
                "1 (not at all) to 10 (very much)",
            "How often do you procrastinate doing tasks that you don’t like, but know must be done? \n" +
                "1=Seldom to 5=Very often",
            "How well do you think you bounce back from difficult situations?\n Not at all well =1, and Extremely Well = 7",
            "In the past 6 months, have you used campus resources to get support for your psychological well-being or mental health?"};

    public static String[] POLL_INDEX = {"0", "1", "2","3","4","5","6"};

    public static String DAY_COUNT_KEY = "daycount";
    public static int DAY_COUNT=3;
}
