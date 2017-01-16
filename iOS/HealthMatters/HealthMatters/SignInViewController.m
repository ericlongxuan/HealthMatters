//
//  SignInViewController.m
//  StudentLife
//
//  Created by Rui Wang on 8/17/15.
//  Copyright (c) 2015 Rui Wang. All rights reserved.
//

#import "SignInViewController.h"
#import "Server.h"
#import "constants.h"

@interface SignInViewController ()

@property (weak, nonatomic) IBOutlet UITextField *txtUsername;
@property (weak, nonatomic) IBOutlet UITextField *txtPassword;

@end

@implementation SignInViewController


- (BOOL)signIn {
    BOOL signedIn = NO;
    
    NSString* username = _txtUsername.text;
    NSString* password = _txtPassword.text;
    
    bool loginResult = [Server SignIn: username password: password];
    
    // Store the data
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    if(loginResult) {
        [defaults setObject:username forKey:SP_USERNAME];
        [defaults setObject:password forKey:SP_PASSWORD];
        [defaults setBool:true forKey:SP_ISREGISTERED];
        
        signedIn = YES;
    } else {
        [defaults setBool:false forKey:SP_ISREGISTERED];
        
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Sign in failed." message:@"Please check your username and password." delegate:self cancelButtonTitle:@"Hide" otherButtonTitles:nil];
        alert.alertViewStyle = UIAlertActionStyleDefault;
        [alert show];
    }
    
    [defaults synchronize];

    return signedIn;
}

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    if ([identifier isEqualToString:@"segueSignedIn"]) {
        bool signedIn = [self signIn];
        if (signedIn) {
            return YES;
        }
        return NO;
    }
    
    return YES;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    _txtUsername.delegate = self;
    _txtPassword.delegate = self;
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)textFieldDidEndEditing:(UITextField *)textField {
    [textField resignFirstResponder];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

@end