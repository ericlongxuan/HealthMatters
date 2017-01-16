//
//  DataUploader.m
//  CampusLife
//
//  Created by Rui Wang on 12/28/15.
//  Copyright © 2015 Rui Wang. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AppDelegate.h"
#import "DataUploader.h"
#import "Reachability.h"
#import "constants.h"
#import "Server.h"

#define UPLOAD_INTERVAL (5 * 60)
@interface DataUploader ()

@property UIDeviceBatteryState  batteryState;
@property BOOL isStarted;
@property(nonatomic, strong) NSLock* uploadLock;
@end

@implementation DataUploader

-(id)init{
    _isStarted = false;
    _uploadLock = [[NSLock alloc] init];
    
    UIDevice *device = [UIDevice currentDevice];
    device.batteryMonitoringEnabled = YES;
    _batteryState = device.batteryState;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(batteryChanged:) name:@"UIDeviceBatteryStateDidChangeNotification" object:device];
    return self;
}

-(void)batteryChanged:(NSNotification*)notification {
    UIDevice* currentDevice = [UIDevice currentDevice];
    _batteryState = [currentDevice batteryState];
    
    NSLog(@"=========battery state changed:%ld", _batteryState);
}

-(void)startPipeline {
    @synchronized(self) {
        if(!_isStarted) {
            [self performSelector:@selector(doDutyCycle) withObject:self afterDelay: UPLOAD_INTERVAL];
        }
        
        _isStarted = true;
    }
}

-(void)stopPipeline {
    @synchronized(self) {
        [NSObject cancelPreviousPerformRequestsWithTarget:self];
        
        self.isStarted = false;
    }
}

-(void)doDutyCycle {
    NSLog(@"try to upload");
    @synchronized(self) {
        [NSObject cancelPreviousPerformRequestsWithTarget:self];
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self uploadData:false];
        });
        
        [self performSelector:@selector(doDutyCycle) withObject:self afterDelay: UPLOAD_INTERVAL];
    }
}

// upload the db files to the server.
- (void)uploadData: (BOOL) force {
    if(![_uploadLock tryLock]) {
        // cannot lock -> other thread is uploading
        NSLog(@"other thread is uploading");
        return;
    }
    
    BOOL canUpload = force;
    
    if(!canUpload){
        BOOL hasWifi = [[Reachability reachabilityForLocalWiFi] currentReachabilityStatus] == ReachableViaWiFi;
        BOOL isCharging = _batteryState == UIDeviceBatteryStateCharging || _batteryState == UIDeviceBatteryStateFull;
        
        canUpload = hasWifi && isCharging;
    }
    
    if(canUpload) {
        NSLog(@"uploading started");
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        bool isSignedIn = [defaults boolForKey:SP_ISREGISTERED];
        
        if (isSignedIn) {
            NSString* username = [defaults objectForKey:SP_USERNAME];
            NSString* password = [defaults objectForKey:SP_PASSWORD];
            
            AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
            NSArray* fileList = [[appDelegate dataManager] getArchiveDbList];
            [Server upload:username password:password filelist:fileList];
        }
        
        NSLog(@"uploading finished");
    }
    
    [_uploadLock unlock];
}

@end
