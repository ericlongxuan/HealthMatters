//
//  InformationViewController.m
//  HealthMatters
//
//  Created by Varun Mishra on 4/3/16.
//  Copyright © 2016 Varun Mishra. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "InformationViewController.h"
#import "UIButton+Bootstrap.h"
#import "Utils.h"
#import "constants.h"
#import "AppDelegate.h"

#import <QuartzCore/QuartzCore.h>

@interface InformationViewController ()

@property (weak, nonatomic) IBOutlet UILabel *txtInfo;
@property (weak, nonatomic) IBOutlet UIImageView *imgView;


@end

@implementation InformationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: 0];
    [[UIApplication sharedApplication] cancelAllLocalNotifications];

    [self.button primaryStyle];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger currentInfo = [defaults integerForKey:LAST_ASKED_INFO_Q];
    int temp = (int)currentInfo+1;
    NSString* path = [NSString stringWithFormat:@"images/s%d.png", temp];
    _imgView.image = [UIImage imageNamed:path];
    _txtInfo.text = [[Utils getInformation] objectAtIndex:currentInfo];

        
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

- (IBAction)showActionSheet:(UIButton *)sender
{
       NSString *alertTitle = NSLocalizedString(@"Seelct Options", @"Seelct Options");
    NSString *alertMessage = NSLocalizedString(@"Like/Dislike/Share", @"Like/Dislike/Share");
    
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:alertTitle
                                                                             message:alertMessage
                                                                      preferredStyle:UIAlertControllerStyleActionSheet];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"Cancel", @"Cancel action")
                                                           style:UIAlertActionStyleCancel
                                                         handler:^(UIAlertAction *action)
                                   {
                                       NSLog(@"Cancel action");
                                   }];
    
    UIAlertAction *shareAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"Share", @"Share action")
                                                           style:UIAlertActionStyleDefault
                                                         handler:^(UIAlertAction *action)
                                   {
                                       NSLog(@"Share action");
                                   }];
    
    UIAlertAction *likeAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"Like", @"Like action")
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction *action)
                                    {
                                        NSLog(@"Like action");
                                    }];
    
    UIAlertAction *dislikeAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"Dislike", @"Dislike action")
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction *action)
                                    {
                                        NSLog(@"Dislike action");

                                    }];
    
    [alertController addAction:cancelAction];
    [alertController addAction:shareAction];
    [alertController addAction:likeAction];
    [alertController addAction:dislikeAction];
    
    UIPopoverPresentationController *popover = alertController.popoverPresentationController;
    if (popover)
    {
        popover.sourceView = sender;
        popover.sourceRect = sender.bounds;
        popover.permittedArrowDirections = UIPopoverArrowDirectionAny;
    }
    
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)didEnterBackground:(NSNotification *)notification
{
    [self.presentedViewController dismissViewControllerAnimated:NO completion:nil];
}


-(void)saveToDb:(int)like {
    NSMutableDictionary *infoData = [[NSMutableDictionary alloc]init];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger currentInfo = [defaults integerForKey:LAST_ASKED_INFO_Q];
    [infoData setValue:[NSString stringWithFormat:@"%ld", (long)currentInfo] forKey:@"infoID"];
    [infoData setValue:[NSString stringWithFormat:@"%d", like] forKey:@"Response"];
    NSError *error;
    
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:infoData
                                                       options:NSJSONWritingPrettyPrinted error:&error];
    
    NSString* info_resp = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *timestamp = [NSString stringWithFormat:@"%.0f",[[NSDate date] timeIntervalSince1970]];
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];

    [appDelegate insertData:EVENT_ID_INFORMATION eventData:info_resp timestamp:timestamp];
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