//
//  AppDelegate.m
//  HealthMatters
//
//  Created by Varun Mishra on 4/3/16.
//  Copyright © 2016 Varun Mishra. All rights reserved.
//
#import <UIKit/UIKit.h>
#import "AppDelegate.h"
#import "constants.h"
#import "Utils.h"
#import "DataUploader.h"
@interface AppDelegate ()
@property (nonatomic, strong) DataUploader* dataUploader;

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    if ([UIApplication instancesRespondToSelector:@selector(registerUserNotificationSettings:)]){
        [application registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert|UIUserNotificationTypeBadge|UIUserNotificationTypeSound categories:nil]];
    }
    self.manager = [RPKManager managerWithDelegate:self];
    [self.manager start];
    self.dataUploader = [[DataUploader alloc] init];
    [self.dataUploader startPipeline];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

- (void) application:(UIApplication *)application performFetchWithCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    // Called when updating applicaiton data in the background. Requires that UIBackgroundModes contains "fetch" in the applications's Info.plist. See PKExample-Info.plist for an example.
    [self.manager syncWithCompletionHandler: completionHandler];
}

-(void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    double lastInfoTime = [defaults doubleForKey:LAST_ASKED_INFO_TIME];
    NSInteger infoCount = [defaults integerForKey:DAY_COUNT_INFO];
    if(infoCount==1){
        [self showPoll];
    } else {
        
        NSLog(@"Camehere-- %f", [Utils getTimestamp].doubleValue);
        if ([Utils getTimestamp].doubleValue-lastInfoTime>TIME_INTERVAL && infoCount<3){
            NSInteger currentInfo = [defaults integerForKey:LAST_ASKED_INFO_Q];
            currentInfo = (currentInfo+1)%16;
           
            [defaults setInteger:currentInfo forKey:LAST_ASKED_INFO_Q];
            [defaults setDouble:[Utils getTimestamp].doubleValue forKey:LAST_ASKED_INFO_TIME];
            UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Information" bundle:nil];
            UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"InformationViewController"];
            
            [self.window setRootViewController:vc];
        }
    }
    infoCount = (infoCount+1);
    [defaults setInteger:infoCount forKey:DAY_COUNT_INFO];

    
}

- (void)forceUploadData {
    [[self dataManager] dumpDb:YES];

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSLog(@"force upload data");
        [self.dataUploader uploadData:true];
    });
}

- (void)insertData:(int)eventID eventData:(NSString *) eventData timestamp: (NSString*) timestampStr {
    if(_dataManager == nil) {
        _dataManager = [[DataManager alloc] init];
        [_dataManager openOrCreateDb];
    }
    
    [[self dataManager] insertRecord:eventID eventData:eventData timestamp:timestampStr];
    [[self dataManager] dumpDb:NO];
}

-(void)showPoll {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger currentInfo = [defaults integerForKey:LAST_ASKED_POLL_Q];
    currentInfo = (currentInfo+1)%7;
    [defaults setInteger:currentInfo forKey:LAST_ASKED_POLL_Q];
    [defaults setDouble:[Utils getTimestamp].doubleValue forKey:LAST_ASKED_INFO_TIME];
    
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Poll" bundle:nil];
    UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"PollViewController"];
    
    [self.window setRootViewController:vc];
    
}
#pragma mark Proximity Kit Delegate Methods

- (void)proximityKitDidSync:(RPKManager *)manager {
    NSLog(@"Did Sync");
}
- (void)proximityKit:(RPKManager *)manager didEnter:(RPKRegion*)region {
    NSLog(@"Entered Region %@ (%@)", region.name, region.identifier);
}

- (void)proximityKit:(RPKManager *)manager didExit:(RPKRegion *)region {
    NSLog(@"Exited Region %@ (%@)", region.name, region.identifier);
}

- (void)proximityKit:(RPKManager *)manager didRangeBeacons:(NSArray *)beacons inRegion:(RPKBeacon *)region
{
    for (RPKBeacon *beacon in beacons) {
        NSLog(@"Ranged UUID: %@ Major:%@ Minor:%@ RSSI:%@", [beacon.uuid UUIDString], beacon.major, beacon.minor, beacon.rssi);
    }
}

- (void)proximityKit:(RPKManager *)manager didDetermineState:(RPKRegionState)state forRegion:(RPKRegion *)region
{
    NSMutableDictionary *beaconData = [[NSMutableDictionary alloc]init];
    
    
    if (state == RPKRegionStateInside) {
        [beaconData setValue:[NSString stringWithFormat:@"1"] forKey:@"State"];
        [beaconData setValue:[NSString stringWithFormat:@"%@", region.name] forKey:@"Region"];

        [self handleBeacon:region];
        
        NSLog(@"State Changed: inside region %@ (%@)", region.name, region.identifier);
    } else if (state == RPKRegionStateOutside) {
        [beaconData setValue:[NSString stringWithFormat:@"0"] forKey:@"State"];
        [beaconData setValue:[NSString stringWithFormat:@"%@", region.name] forKey:@"Region"];

        NSLog(@"State Changed: outside region %@ (%@)", region.name, region.identifier);
    } else if (state == RPKRegionStateUnknown) {
        [beaconData setValue:[NSString stringWithFormat:@"-1"] forKey:@"State"];
        [beaconData setValue:[NSString stringWithFormat:@"%@", region.name] forKey:@"Region"];

        NSLog(@"State Changed: unknown region %@ (%@)", region.name, region.identifier);
    }
    NSError *error;

    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:beaconData
                                                       options:NSJSONWritingPrettyPrinted error:&error];
    
    NSString* beacon_resp = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *timestamp = [NSString stringWithFormat:@"%.0f",[[NSDate date] timeIntervalSince1970]];
    [self insertData:EVENT_ID_BEACON_REGION eventData:beacon_resp timestamp:timestamp];

}

- (void)proximityKit:(RPKManager *)manager didFailWithError:(NSError *)error
{
    NSLog(@"Error: %@", error.description);
}

#pragma mark Helper Methods

- (void) alert:(NSString *)format, ...
{
    va_list args;
    va_start(args, format);
    NSString *str = [[NSString alloc] initWithFormat:format arguments:args];
    va_end(args);
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle: @"Proximity Kit"
                                                    message: [NSString stringWithFormat:@"%@", str]
                                                   delegate: nil
                                          cancelButtonTitle: @"OK"
                                          otherButtonTitles: nil];
    [alert show];
}

- (void) handleBeacon:(RPKRegion *)region
{
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger infoCount = [defaults integerForKey:DAY_COUNT_INFO];
    double lastInfoTime = [defaults doubleForKey:LAST_ASKED_INFO_TIME];
    NSDate *lastdate = [NSDate dateWithTimeIntervalSince1970:lastInfoTime];

    NSDateComponents *lastcomponents = [[NSCalendar currentCalendar] components:NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear fromDate:lastdate];
    NSInteger lastday = [lastcomponents day];
    NSDate *newdate = [NSDate dateWithTimeIntervalSince1970:[Utils getTimestamp].doubleValue];
    NSDateComponents *newcomponents = [[NSCalendar currentCalendar] components:NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear fromDate:newdate];
    NSInteger today = [newcomponents day];
    NSLog(@"Last day- %ld, today day - %ld",lastday, today);
    if(today>lastday){
        [defaults setInteger:0 forKey:DAY_COUNT_INFO];

    }

    if([Utils getTimestamp].doubleValue-lastInfoTime>TIME_INTERVAL && infoCount<3){
        UILocalNotification* localNotification = [[UILocalNotification alloc] init];
        localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:1];
        localNotification.alertBody = @"New Information";
        localNotification.soundName = UILocalNotificationDefaultSoundName;
        localNotification.timeZone = [NSTimeZone defaultTimeZone];
        [[UIApplication sharedApplication] presentLocalNotificationNow:localNotification];
        
    }
    
   

}

@end
