//
//  ViewController.m
//  HealthMatters
//
//  Created by Varun Mishra on 4/3/16.
//  Copyright © 2016 Varun Mishra. All rights reserved.
//

#import "ViewController.h"
#import "constants.h"
#import "Utils.h"
#import "UIButton+Bootstrap.h"
#import "AppDelegate.h"
@interface ViewController ()
@property (weak, nonatomic) IBOutlet UILabel *txtSignInUsrInfo;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    [self.uploadButton primaryStyle];
    [self.dumpButton primaryStyle];
    [self.ViewQuestion primaryStyle];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    bool isSignedIn = [defaults boolForKey:SP_ISREGISTERED];
    if (isSignedIn) {
        NSString* username = [defaults objectForKey:SP_USERNAME];
        [_button setEnabled:NO];
        [self.button defaultStyle];
        _txtSignInUsrInfo.text = [NSString stringWithFormat:@"User signed in: %@", username];
    } else {
        [_button setEnabled:YES];
        [self.button primaryStyle];
        _txtSignInUsrInfo.text = @"Please sign in now -- your data is not being uploaded";
    }


}

-(IBAction)onSignInClicked:(id)sender {
    
}

- (IBAction)onViewQuestionsClick:(id)sender{
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    double lastInfoTime = [defaults doubleForKey:LAST_ASKED_INFO_TIME];
    NSLog(@"Camehere-- %f", [Utils getTimestamp].doubleValue);
    if ([Utils getTimestamp].doubleValue-lastInfoTime>TIME_INTERVAL){
        NSInteger currentInfo = [defaults integerForKey:LAST_ASKED_INFO_Q];
        currentInfo = (currentInfo+1)%16;
        [defaults setInteger:currentInfo forKey:LAST_ASKED_INFO_Q];
        [defaults setDouble:[Utils getTimestamp].doubleValue forKey:LAST_ASKED_INFO_TIME];
        UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Information" bundle:nil];
        UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"InformationViewController"];
        
        [self presentViewController:vc animated:YES completion:nil];
        
    }
}

- (IBAction)onDumpClick:(id)sender {
   /* AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    DataManager *dm = appDelegate.dataManager;
    
    [dm dumpDb:YES];
*/
    }

- (IBAction)onUploadClick:(id)sender {
   

    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    DataManager *dm = appDelegate.dataManager;
    
    [dm dumpDb:YES];
    [appDelegate forceUploadData];
   
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
