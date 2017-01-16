//
//  DataManager.h
//  StudentLife
//
//  Created by Rui Wang on 8/20/15.
//  Copyright (c) 2015 Rui Wang. All rights reserved.
//

#ifndef StudentLife_DataManager_h
#define StudentLife_DataManager_h

#include <sqlite3.h>

@interface DataManager : NSObject {
    sqlite3 *_database;
}

-(id)init;
-(void)openOrCreateDb;
-(void)closeDb;
-(void)dumpDb: (BOOL) force;

-(void)insertRecord: (int)eventID eventData:(NSString *) eventData timestamp: (NSString*) timestampStr;
-(void)insertRecord: (int)eventID eventData:(Byte *) eventData withLen:(int)len timestamp: (NSString*) timestampStr;

#if 0
-(void)insertEMARecord: (NSString*)timestampStr sse3ques1:(int) sse3q1 sse3ques2:(int) sse3q2 sse3ques3:(int) sse3q3 sse3ques4:(int) sse3q4 phq4ques1:(int) phq4q1 phq4ques2:(int) phq4q2 phq4ques3:(int) phq4q3 phq4ques4:(int) phq4q4 studyques:(int) study partyques:(int) party;
-(void)insertPAMRecord: (NSString*)timestampStr pam:(NSString*) pamData;
#endif

-(NSArray *)getArchiveDbList;

+(NSString*)getLiveDbDir;
+(NSString*)getArchiveDbDir;

@end

#endif
