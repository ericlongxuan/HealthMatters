//
//  FeedbackViewController.m
//  HealthMatters
//
//  Created by Varun Mishra on 4/4/16.
//  Copyright © 2016 Varun Mishra. All rights reserved.
//

#import "FeedbackViewController.h"
#import "UIButton+Bootstrap.h"
#import "constants.h"
#import "AppDelegate.h"
@interface FeedbackViewController ()

@property (weak, nonatomic) IBOutlet UILabel *txtFeedback;

@end
@implementation FeedbackViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.button primaryStyle];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString* response = [defaults objectForKey:POLL_RESPONSE];
    _txtFeedback.text = response;
    
    
}
-(IBAction)doneButton:(UIButton *)sender{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"MainViewController"];
    [self presentViewController:vc animated:YES completion:nil];
}

-(IBAction)likeButton {
    [self saveToDb:1];
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"MainViewController"];
    [self presentViewController:vc animated:YES completion:nil];
    
}

-(IBAction)dislikeButton {
    [self saveToDb:0];

    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"MainViewController"];
    [self presentViewController:vc animated:YES completion:nil];
    
}

-(IBAction)shareButton {
    [self saveToDb:2];

    [self handleShare];
    
}


-(void)saveToDb:(int)like {
    NSMutableDictionary *pollFeedback = [[NSMutableDictionary alloc]init];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString* response = [defaults objectForKey:POLL_RESPONSE];

    [pollFeedback setValue:[NSString stringWithFormat:@"%@", response] forKey:@"feedback"];
    [pollFeedback setValue:[NSString stringWithFormat:@"%d", like] forKey:@"Response"];
    NSError *error;
    
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:pollFeedback
                                                       options:NSJSONWritingPrettyPrinted error:&error];
    
    NSString* poll_feed_resp = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *timestamp = [NSString stringWithFormat:@"%.0f",[[NSDate date] timeIntervalSince1970]];
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    
    [appDelegate insertData:EVENT_ID_POLL_FEEDBACK eventData:poll_feed_resp timestamp:timestamp];
    if (like<2){
        [appDelegate forceUploadData];

    }
    
}

-(void)handleShare {
    
    UIGraphicsBeginImageContext(self.view.bounds.size);
    [self.view.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *snapShotImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    NSLog(@"shareButton pressed");
    NSString *texttoshare = @"Interesting";
    UIImage *imagetoshare = snapShotImage;
    NSArray *activityItems = @[texttoshare, imagetoshare];
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:activityItems applicationActivities:nil];
    activityVC.excludedActivityTypes = @[UIActivityTypeAssignToContact, UIActivityTypePrint, UIActivityTypePostToTwitter, UIActivityTypePostToWeibo];
    [self presentViewController:activityVC animated:TRUE completion:nil];
}

@end
