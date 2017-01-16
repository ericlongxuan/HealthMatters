//
//  Utils.m
//  StudentLife
//
//  Created by Rui Wang on 8/22/15.
//  Copyright (c) 2015 Rui Wang. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Utils.h"

@implementation Utils

+(NSString*) getTimestamp {
    NSString *currentTimestamp = [NSString stringWithFormat:@"%1.0f", [[NSDate date] timeIntervalSince1970]];
    
    return currentTimestamp;
}
+(NSArray*) getInformation {
    NSArray *data= [NSArray arrayWithObjects: @"Change your Attitude to Gratitude.  What are 5 things you are grateful for today?",@"Think about 3 people or things that make you truly happy.",@"Do your little bit of good where you are. It's those little bits of good put together that overwhelm the world. \n- Desmond Tutu - South African social rights activist and former Archbishop",@"We do not need magic to change the world\n We carry all the power we need inside ourselves already\n We have the power to imagine better \n- JK Rowling, author",@" Focus on breathing and grounding yourself.   Breathe in calmness and breathe out nervousness. Trust your inner wisdom and intuition. Trust yourself.",@"Practice random acts of kindness. It’s good for your health!",@"Done is better than perfect. \nSheryl Sandberg - Chief Operating Officer, Facebook",@"Mindfulness =\n Paying attention on purpose, in the present moment, and nonjudgmentally \n - Jon-Kabat-Zinn",@"Stop for a moment. Look around. \nWhat are thoughts, feelings, sounds, and smells in this moment.?",@"Be as compassionate with yourself as you would with a puppy. When you notice that your mind has wandered, gently and kindly guide it back to the present moment.",@"Lost in thoughts or worries? Stop and take a deep breath, and bring your attention to this moment. This will give your brain a break.",@"Simplify. You can do anything, but not everything.",@"Think deeply about habits that guide your daily actions.\n Have the courage to lose sight of old habits to make room for new ones to begin!",@"Hydrate! Drinking >10 glasses of water a day helps combat fatigue.",@"Sleep is when brains and bodies rebuild and repair. Tune into your bedtime and waketime each day.",@"Feeling depleted?\n Try recharging with natural alternatives: work out, space out meals, walk outside, hydrate, get sleep, stretch, meditate.", nil];
    
    return data;
    
}

+(NSArray*) getPoll {
    NSArray *data= [NSArray arrayWithObjects: @"How much sleep did you get last night? \n Response scale: 0 to 10+ hours \n",@"How stressed do you feel now? Rate your stress.  \n Response scale:  0 ~ 10\n",@"How happy did you feel yesterday?\n Not at all happy = 1,  Very happy = 10",@"How well have your nourished yourself today?\n 1 (not at all) to 10 (very much)",@"How often do you procrastinate doing tasks that you don’t like, but know must be done?\n1=Seldom to 5=Very often",@"How well do you think you bounce back from difficult situations?\n Not at all well =1, and Extremely Well = 7",@"Do you think there are enough resources on campus to help students when they are feeling stressed out?", nil];
    
    return data;
    
}
@end