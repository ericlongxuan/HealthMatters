//
//  AppDelegate.h
//  HealthMatters
//
//  Created by Varun Mishra on 4/3/16.
//  Copyright © 2016 Varun Mishra. All rights reserved.
//

#import <UIKit/UIKit.h>


#import "DataManager.h"
#import <ProximityKit/ProximityKit.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate, RPKManagerDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (strong) DataManager *dataManager;


//
// Create a property for the proximity Kit manager
//
@property (strong, nonatomic) RPKManager *manager;

- (void) alert:(NSString *)msg, ... NS_FORMAT_FUNCTION(1,2);
- (void)insertData:(int)eventID eventData:(NSString *) eventData timestamp: (NSString*) timestampStr;

- (void)forceUploadData;

@end

