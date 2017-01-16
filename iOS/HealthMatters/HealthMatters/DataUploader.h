//
//  DataUploader.h
//  CampusLife
//
//  Created by Rui Wang on 12/28/15.
//  Copyright © 2015 Rui Wang. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DataUploader : NSObject

-(id)init;
-(void)startPipeline;
-(void)stopPipeline;
-(void)doDutyCycle;
-(void)uploadData: (BOOL) force;

@end
