//
//  PollViewController.m
//  HealthMatters
//
//  Created by Varun Mishra on 4/4/16.
//  Copyright © 2016 Varun Mishra. All rights reserved.
//

#import "PollViewController.h"
#import "Utils.h"
#import "constants.h"
#import "FeedbackViewController.h"
#import "AppDelegate.h"
@import ResearchKit;

@interface PollViewController () <ORKTaskViewControllerDelegate>

@property (strong, nonatomic)  ORKTaskViewController *taskViewController;

@end
@implementation PollViewController
- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger answered = [defaults integerForKey:ANSWERED_POLL];
    if (answered==1) {
        [defaults setInteger:0 forKey:ANSWERED_POLL];

        UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Poll" bundle:nil];
        UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"FeedbackViewController"];
        
        [self presentViewController:vc animated:YES completion:nil];

    } else {
    ORKOrderedTask *task = [self generateOrderedTask];
    ORKTaskViewController *taskViewController = [[ORKTaskViewController alloc] initWithTask:task taskRunUUID:nil];
    taskViewController.delegate = self;
    [self presentViewController:taskViewController animated:YES completion:nil];
    }
}

#pragma survey - Survey Setup

- (ORKOrderedTask *)generateOrderedTask {
    
    ORKQuestionStep *pollQuestion = [self generatePollQuestion];
    ORKOrderedTask *task = [[ORKOrderedTask alloc] initWithIdentifier:@"PollQuestions" steps:@[pollQuestion]];
    return task;
}

- (ORKQuestionStep *)generatePollQuestion {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger currentPoll = [defaults integerForKey:LAST_ASKED_POLL_Q];
    if (currentPoll==6) {
      
        ORKBooleanAnswerFormat *format = [ORKBooleanAnswerFormat alloc];

        ORKQuestionStep *step = [ORKQuestionStep questionStepWithIdentifier:@"pollQuestion"
                                                                      title:[[Utils getPoll] objectAtIndex:currentPoll]
                                                                     answer:format];
        step.optional = NO;

        return step;
    }
    
    ORKScaleAnswerFormat *format;
    if (currentPoll==4) {
       format = [[ORKScaleAnswerFormat alloc] initWithMaximumValue:5 minimumValue:1 defaultValue:3 step:1];
    } else if(currentPoll==5) {
        format = [[ORKScaleAnswerFormat alloc] initWithMaximumValue:7 minimumValue:1 defaultValue:3 step:1];

    } else {
        format = [[ORKScaleAnswerFormat alloc] initWithMaximumValue:10 minimumValue:0 defaultValue:5 step:1];

    }
    ORKQuestionStep *step = [ORKQuestionStep questionStepWithIdentifier:@"pollQuestion"
                                                                  title:[[Utils getPoll] objectAtIndex:currentPoll]
                                                                 answer:format];
    step.optional = NO;
    
    return step;
}



#pragma mark - ORKTaskViewControllerDelegate

- (void)taskViewController:(ORKTaskViewController *)taskViewController
       didFinishWithReason:(ORKTaskViewControllerFinishReason)reason
                     error:(NSError *)error {
    
    switch (reason) {
        case ORKTaskViewControllerFinishReasonCompleted: {
            [self handleResultsForCompletedTaskViewController:taskViewController];
            
            break;
        }
        default:
            break;
    }
    [self dismissViewControllerAnimated:YES completion:nil];
}
- (void)taskViewController:(ORKTaskViewController *)taskViewController
stepViewControllerWillAppear:(ORKStepViewController *)stepViewController {
    
    

        stepViewController.cancelButtonItem = nil;

}

#pragma mark - ORKTaskViewControllerDelegate Helpers

- (void)handleResultsForCompletedTaskViewController:(ORKTaskViewController *)taskViewController {
    NSNumber *pollAnswer;
    
    NSArray *stepsResults = taskViewController.result.results;
    for (ORKStepResult *stepResult in stepsResults) {
        ORKQuestionResult *questionResult = stepResult.results.firstObject;
        switch (questionResult.questionType) {
            case ORKQuestionTypeBoolean:
                pollAnswer = [self parseValueFromBooleanResult:questionResult];
                break;
            case ORKQuestionTypeScale:
                pollAnswer = [self parseValueFromScaleResult:questionResult];
                break;
            default:
                break;
        }
    }
//    
//    NSString *message = [NSString stringWithFormat:@"You answered- %@",[pollAnswer stringValue]];
//    UIAlertView *alterView = [[UIAlertView alloc] initWithTitle:@"Result" message:message delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:nil];
//    [alterView show];
    [self handleResponse:pollAnswer];
    [self saveToDb:pollAnswer];
   
}

-(void)handleResponse:(NSNumber*)tmpvalue {
    NSString* response = @"";
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger id = [defaults integerForKey:LAST_ASKED_POLL_Q];
    NSInteger value = [tmpvalue integerValue];
    if(id==0){
        if (value<7) {
            response = @"Trouble sleeping?\nClose the book, turn off devices, and put on some socks. Did you know that warm hands and feet are associated with falling asleep more quickly?\nAnd try these study tips:\n• Study during periods of optimal brain function (usually around 6-8 p.m.)\n• Avoid studying in early afternoons, usually the time of least alertness";

        }else {
            response=@"69% of Dartmouth students, on average, report getting 7 or more hours a sleep per night.";
        }
    } else if(id==1){
        if(value<5){
            response=@"To maintain a healthy level of stress, practice deep breathing to rest your brain and focus in the moment.";
        } else if(value>=5 && value<=7){
            response=@"Take a few deeeeeep breaths to rest your brain and focus in the moment.";
        } else if(value>7){
            response = @"Take a few deeeeeep breaths to rest your brain and focus in the moment. Consider stopping by the Student Wellness Center for a check-In or make an appointment with Counseling and Human Development";
        }
    } else if(id==2){
        response = @"What can you do today to boost your happiness level?";
    } else if(id==3){
        response=@"Over half of Dartmouth students eat at least 3 servings of fruits and vegetables a day.";
    } else if(id==4){
        if (value<4) {
            response = @"You’re not alone!  About 1 out of 4 Dartmouth students report that they don’t usually put off tasks that need to be done, even if they don’t like them!";
        }else {
            response = @"You’re not alone! Over half of Dartmouth students report procrastinating on tasks they don’t like. \nAnd students with this tendency reported feeling very nervous and stressed in the last past 30 days.";
            
        }
    } else if (id==5){
        if (value<4) {
            response = @"Bouncing back from challenges can be hard. Finding trusted support can help, like friends or resources on campus.Consider checking out Counseling and Human Development at Dick’s House to see if there may be helpful resources for you.";
        }else if(value>3 && value<6){
            response=@"Think about times you’ve felt good about managing a challenge.  What helped to get you through? Draw on what’s worked in the past to get through the tough times. ";
        } else{
            response=@"Good for you!  You’ve figured out what works for you to bounce back from tough stuff. Use these resources when things get challenging in the future.";
            
        }
        
    } else{
        response=@"About 1 out of 5 Dartmouth students have used campus resources for their psychological well-being or mental health in the last 6 months.  Support is there if you need it.  ";
        
    }
    [defaults setObject:response forKey:POLL_RESPONSE];
    [defaults setInteger:1 forKey:ANSWERED_POLL];
    }

- (NSNumber *)parseValueFromScaleResult:(ORKQuestionResult *)questionResult {
    if ([questionResult isKindOfClass:[ORKScaleQuestionResult class]]) {
        return [(ORKScaleQuestionResult *)questionResult scaleAnswer];
    }
    return nil;
}

- (NSNumber *)parseValueFromBooleanResult:(ORKQuestionResult *)questionResult {
    if ([questionResult isKindOfClass:[ORKBooleanQuestionResult class]]) {
        ORKBooleanQuestionResult *booleanResult = questionResult;
        NSNumber *booleanAnswer = booleanResult.booleanAnswer;
        return booleanAnswer;
    }
    return nil;
}

- (NSString *)timeFromDate:(NSDate *)date {
    NSDateFormatter *timeFormatter = [[NSDateFormatter alloc] init];
    timeFormatter.dateFormat = @"HH:mm";
    
    return [timeFormatter stringFromDate:date];
}

-(void)saveToDb:(NSNumber *)pollAnswer {
    NSMutableDictionary *infoData = [[NSMutableDictionary alloc]init];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger currentInfo = [defaults integerForKey:LAST_ASKED_POLL_Q];
    NSInteger value = [pollAnswer integerValue];

    [infoData setValue:[NSString stringWithFormat:@"%ld", (long)currentInfo] forKey:@"infoID"];
    [infoData setValue:[NSString stringWithFormat:@"%ld", (long)value] forKey:@"Response"];
    NSError *error;
    
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:infoData
                                                       options:NSJSONWritingPrettyPrinted error:&error];
    
    NSString* info_resp = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *timestamp = [NSString stringWithFormat:@"%.0f",[[NSDate date] timeIntervalSince1970]];
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    
    [appDelegate insertData:EVENT_ID_POLL eventData:info_resp timestamp:timestamp];
}


@end
