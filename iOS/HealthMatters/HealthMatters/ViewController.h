//
//  ViewController.h
//  HealthMatters
//
//  Created by Varun Mishra on 4/3/16.
//  Copyright © 2016 Varun Mishra. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController

-(IBAction)onSignInClicked:(id)sender;
@property (nonatomic, weak) IBOutlet UIButton* button;
@property (nonatomic, weak) IBOutlet UIButton* uploadButton;
@property (nonatomic, weak) IBOutlet UIButton* dumpButton;
@property (weak, nonatomic) IBOutlet UIButton *ViewQuestion;
@end

