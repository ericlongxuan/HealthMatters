//
//  DataManager.m
//  StudentLife
//
//  Created by Rui Wang on 8/20/15.
//  Copyright (c) 2015 Rui Wang. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "DataManager.h"

#define DB_LIVE_DIR @"live_db"
#define DB_LIVE_FILENAME @"healthmatterdb.db"

#define DB_ARCHIVE_DIR @"archive_db"
#define DB_ARCHIVE_FILENAME_FORMAT @"%@_healthmatters.db"

#define DB_CREATETIMESTAMP @"DB_CREATETIMESTAMP"

// dump the database every 20 min
#define DB_DUMP_TIME (20 * 60)

#define MAX_INSERT_NUM  1000
#define MAX_DB_ERROR_NUM  50
@interface DataManager ()

@property sqlite3* ActiveDatabase;
@property(nonatomic, strong) dispatch_queue_t dbWriteQueue;
@property int insertCount;
@property int dbErrorNum;
@end

@implementation DataManager

@synthesize ActiveDatabase;

- (id)init {
    if ((self = [super init])) {
        // init queue
        _dbWriteQueue = dispatch_queue_create("edu.dartmouth.cs.HealthMatters.dbQueue", NULL);
        // create db dirs
        [self createDirectory:[DataManager getLiveDbDir]];
        [self createDirectory:[DataManager getArchiveDbDir]];
        
        int err = sqlite3_config(SQLITE_CONFIG_SINGLETHREAD);
        if(err != SQLITE_OK) {
            NSLog(@"sqlite3_config error:%d", err);
        }
        
        ActiveDatabase = nil;
    }
    return self;
}

-(void)openOrCreateDb {
    dispatch_async(_dbWriteQueue, ^{
        [self _openOrCreateDb];
    });
    
}

-(void)_openOrCreateDb {
    [self createDirectory:[DataManager getLiveDbDir]];
    
    NSString* dbFilePath = [self getDbFilePath];
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:dbFilePath]) {
        int ret = sqlite3_open([dbFilePath UTF8String], &ActiveDatabase);
        if (ret != SQLITE_OK) {
            NSLog(@"Failed to open database!");
        }
    } else {
        int ret = sqlite3_open([dbFilePath UTF8String], &ActiveDatabase);
        if (ret != SQLITE_OK) {
            NSLog(@"Failed to open database!");
            return;
        }
        
        char *errMsg;
        const char *sql_stmt = "CREATE TABLE IF NOT EXISTS "
        "events(event_id INTEGER ASC, event_time DATETIME,event_source_id INTEGER, "
        "event_source_message_type_id INTEGER, sync_id INTEGER, event_data BLOB);";
        
        if (sqlite3_exec(ActiveDatabase, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
            NSLog(@"Failed to create table: %s", errMsg);
        } else {
            [[NSUserDefaults standardUserDefaults] setDouble:[[NSDate date] timeIntervalSince1970]
                                                      forKey:DB_CREATETIMESTAMP];
        }
              
    }
    
    _insertCount = 0;
    _dbErrorNum = 0;
    sqlite3_exec(ActiveDatabase, "BEGIN EXCLUSIVE TRANSACTION", 0, 0, 0);
}

-(void)closeDb {
    if (ActiveDatabase != nil) {
        sqlite3_exec(ActiveDatabase, "COMMIT TRANSACTION", 0, 0, 0);
        sqlite3_close(ActiveDatabase);
        ActiveDatabase = nil;
    }
}

-(void)dumpDb: (BOOL) force {
    double currentTimestamp =[[NSDate date] timeIntervalSince1970];
    double dbTimestamp = [[NSUserDefaults standardUserDefaults] doubleForKey:DB_CREATETIMESTAMP];
    if(dbTimestamp == 0) {
        dbTimestamp = currentTimestamp;
    }
    
    dispatch_async(_dbWriteQueue, ^{
        if([self isActiveDbExists] && ((currentTimestamp - dbTimestamp) > DB_DUMP_TIME || force == YES)) {
            [self dumpDb];
        }
    });
    
}

-(void)dumpDb{
    [self createDirectory:[DataManager getArchiveDbDir]];
    [self closeDb];
    NSString* from = [self getDbFilePath];
    NSString* to = [self getNextArchiveDbFilePath];
    
    [[NSFileManager defaultManager] moveItemAtPath: from toPath: to error: nil];
    
    [self _openOrCreateDb];
}

-(void)insertRecord: (int)eventID eventData:(NSString *) eventData timestamp: (NSString*) timestampStr{
    const char *UTF8String = [eventData UTF8String];
    const int strLen = (int)[eventData length];

    [self insertRecord:eventID eventData:(Byte*)UTF8String withLen:strLen timestamp:timestampStr];
}

-(void)insertRecord: (int)eventID eventData:(Byte *) eventData withLen:(int)len timestamp: (NSString*) timestampStr{
    NSData* tmpEventData = [NSData dataWithBytes:eventData length:len];
    
    dispatch_async(_dbWriteQueue, ^{
        const char* sql = "INSERT INTO events "
        "(event_id,event_time, event_source_id, event_source_message_type_id,sync_id, event_data) "
        "VALUES (?, ?, ?, ?, ?, ?)";
        sqlite3_stmt *statement;
        int err = 0;
        // Prepare the statement.
        err = sqlite3_prepare_v2(ActiveDatabase, sql, -1, &statement, NULL);
        if (err == SQLITE_OK) {
            // Bind the parameters (note that these use a 1-based index, not 0).
            sqlite3_bind_int(statement, 1, 0);
            sqlite3_bind_text(statement, 2, [timestampStr UTF8String], (int)[timestampStr length], SQLITE_TRANSIENT);
            sqlite3_bind_int(statement, 3, eventID);
            sqlite3_bind_int(statement, 4, 0);
            sqlite3_bind_int(statement, 5, 0);
            sqlite3_bind_blob(statement, 6, tmpEventData.bytes, len, SQLITE_TRANSIENT);
        }
        
        // Execute the statement.
        if (sqlite3_step(statement) != SQLITE_DONE) {
            _dbErrorNum++;
        }
        
        if(_dbErrorNum > MAX_DB_ERROR_NUM) {
            NSLog(@"Failed to insert binary data, flush db");
            [self dumpDb];
        }
        
        // Clean up and delete the resources used by the prepared statement.
        sqlite3_finalize(statement);
        
        _insertCount++;
        if(_insertCount >= MAX_INSERT_NUM) {
            sqlite3_exec(ActiveDatabase, "COMMIT TRANSACTION", 0, 0, 0);
            NSLog(@"write batch data to db");
            
            _insertCount = 0;
            sqlite3_exec(ActiveDatabase, "BEGIN EXCLUSIVE TRANSACTION", 0, 0, 0);
        }
    });
}

-(NSArray *)getArchiveDbList {
    NSArray * dbList = nil;
    
    NSString* archiveDir = [DataManager getArchiveDbDir];
    dbList = [[NSFileManager defaultManager] contentsOfDirectoryAtPath: archiveDir error:nil];
    
    return dbList;
}


-(void)createDirectory:(NSString *)bundlePath
{
    NSError * error = nil;
    [[NSFileManager defaultManager] createDirectoryAtPath:bundlePath
                              withIntermediateDirectories:YES
                                               attributes:nil
                                                    error:&error];
    if (error != nil) {
        NSLog(@"error creating directory: %@", error);
    }
}

-(BOOL)isActiveDbExists {
    NSString* dbFilePath = [self getDbFilePath];
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:dbFilePath]) {
        return YES;
    }
    
    return NO;
}

-(NSString*)getDbFilePath {
    NSString *docsDir = [DataManager getLiveDbDir];
    
    // Build the path to the database file
    NSString* databasePath = [[NSString alloc] initWithString: [docsDir stringByAppendingPathComponent: DB_LIVE_FILENAME]];
    
    return databasePath;
}

-(NSString*)getNextArchiveDbFilePath {
    NSString *docsDir = [DataManager getArchiveDbDir];
    NSString *currentTimestamp = [NSString stringWithFormat:@"%1.0f", [[NSDate date] timeIntervalSince1970]];
    
    NSString* archiveFilenanme = [NSString stringWithFormat:DB_ARCHIVE_FILENAME_FORMAT, currentTimestamp];
    
    // Build the path to the database file
    NSString* databasePath = [[NSString alloc] initWithString: [docsDir stringByAppendingPathComponent: archiveFilenanme]];
    
    return databasePath;
}

+(NSString*)getLiveDbDir {
    NSString *docsDir;
    NSArray *dirPaths;
    // Get the documents directory
    dirPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    docsDir = [dirPaths objectAtIndex:0];
    // Build the path to the database file
    NSString* liveDbDir = [[NSString alloc] initWithString: [docsDir stringByAppendingPathComponent: DB_LIVE_DIR]];
    
    return liveDbDir;
}

+(NSString*)getArchiveDbDir {
    NSString *docsDir;
    NSArray *dirPaths;
    // Get the documents directory
    dirPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    docsDir = [dirPaths objectAtIndex:0];
    // Build the path to the database file
    NSString* archiveDbDir = [[NSString alloc] initWithString: [docsDir stringByAppendingPathComponent: DB_ARCHIVE_DIR]];
    
    return archiveDbDir;
}

@end
