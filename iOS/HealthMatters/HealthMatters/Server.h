//
//  Server.h
//  StudentLife
//
//  Created by Rui Wang on 8/20/15.
//  Copyright (c) 2015 Rui Wang. All rights reserved.
//

#ifndef StudentLife_Server_h
#define StudentLife_Server_h

@interface Server : NSObject

+ (bool) SignIn : (NSString *) username password:(NSString *) password;

+ (void) upload : (NSString *) username password:(NSString *) password filelist:(NSArray*) filelist;

@end


#endif
