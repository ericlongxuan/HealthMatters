//
//  Server.m
//  StudentLife
//
//  Created by Rui Wang on 8/20/15.
//  Copyright (c) 2015 Rui Wang. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Server.h"
#import "DataManager.h"
@import MobileCoreServices;

#define DEFAULT_SP_DOMAIN_NAME @"biorhythm.cs.dartmouth.edu"
#define DEFAULT_SP_LOGIN_URL [NSString stringWithFormat:@"https://%@/remoteLogin", DEFAULT_SP_DOMAIN_NAME];
#define DEFAULT_SP_UPLOAD_DB_URL [NSString stringWithFormat:@"https://%@/upload", DEFAULT_SP_DOMAIN_NAME];
#define DEFAULT_SP_REGISTER_URL [NSString stringWithFormat:@"https://%@/register", DEFAULT_SP_DOMAIN_NAME];

@interface Server ()

@end

@implementation Server

+ (bool) SignIn : (NSString *) username password:(NSString *) password {
    bool result = false;
    if (username.length == 0 || password.length == 0) {
        return false;
    }
    
    NSString* urlStr = DEFAULT_SP_LOGIN_URL;
    NSURL *url = [NSURL URLWithString:urlStr];
    
    /*  Preparing the data to dictionary  */
    
    NSMutableDictionary *dictionary = [NSMutableDictionary new];
    [dictionary setValue: username forKey:@"email"];
    [dictionary setValue: password forKey:@"password"];
    
    /* Convert request to JSON */
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: dictionary
                                                       options: NSJSONWritingPrettyPrinted
                                                         error: &error];
    
    if (error != nil) {
        return false;
    }
    //NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod:@"POST"];
    [request setHTTPBody: jsonData];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:[NSString stringWithFormat:@"%lu", (unsigned long)[jsonData length]] forHTTPHeaderField:@"Content-Length"];
    request.timeoutInterval = 10;
    
    /* Send HTTP request */
    NSURLResponse * response = nil;
    NSData * data = [NSURLConnection sendSynchronousRequest:request
                                          returningResponse:&response
                                                      error:&error];
    
    if (error == nil) {
        NSError *respError = nil;
        NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&respError];
        if (respError == nil) {
            NSString* resp = [dictionary objectForKey:@"result"];
            if(resp != nil && [resp isEqualToString:@"SUCCESS"]) {
                result = true;
            } else {
                NSLog(@"Sign in failed: %@", [dictionary objectForKey:@"message"]);
            }
        }
    }
    
    return result;
}

+ (void) upload : (NSString *) username password:(NSString *) password filelist:(NSArray*) filelist {
    NSString* archiveDir = [DataManager getArchiveDbDir];

    for (id file in filelist) {
        NSString* filename = [[NSString alloc] initWithString: [archiveDir stringByAppendingPathComponent: (NSString*)file]];
        [Server uploadFile:username password:password file:filename completion:^(bool success, NSError *error) {
            if (success) {
                NSLog(@"Uploaded");
                [[NSFileManager defaultManager] removeItemAtPath:filename error:nil];
            }
        }];
 
            
    }
}

+ (void) uploadFile : (NSString *) username password:(NSString *) password file:(NSString*) filename completion:(void (^)(bool success, NSError *error))completion
 {
    
    NSString* uploadBaseUrl = DEFAULT_SP_UPLOAD_DB_URL;
    NSURL* uploadUrl = [NSURL URLWithString:[NSString stringWithFormat:@"%@/%@/%@",
                                             uploadBaseUrl, username, password]];
    
    NSString *boundary = [self generateBoundaryString];
    
    NSDictionary *params = @{@"comment": [filename lastPathComponent]};
    
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:uploadUrl];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    
    // set content type
    
    NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundary];
    [request setValue:contentType forHTTPHeaderField: @"Content-Type"];
    
    // create body
    NSData *httpBody = [Server createBodyWithBoundary:boundary parameters:params paths:@[filename] fieldName:@"data"];
    
    // send the file
    request.HTTPBody = httpBody;
    
    NSError* error;
    NSURLResponse * response = nil;
   // NSData * data = [NSURLConnection sendSynchronousRequest:request
                                  //        returningResponse:&response
                                     //                 error:&error];
  //  NSURLRequest *urlRequest = [NSURLRequest requestWithURL:uploadUrl];
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
     __block BOOL result = NO;
    [NSURLConnection sendAsynchronousRequest:request queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *error)
     {
        if ([data length] > 0 && error == nil)
        {
            NSError *respError = nil;
            NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&respError];
          
            if (respError == nil) {
                NSString* resp = [dictionary objectForKey:@"result"];
                
                if(resp != nil && [resp isEqualToString:@"SUCCESS"]) {
                    
                    result= YES;
                    if (completion)
                        completion(true, respError);

                } else {
                    NSLog(@"uploading %@ failed: %@", filename, [dictionary objectForKey:@"message"]);
                    if (completion)
                        completion(false, respError);

                }
            }
            else{
                if (completion)
                    completion(false, respError);
            }
        }
       
    }];
     
     [queue waitUntilAllOperationsAreFinished];
    /*
    if (error == nil) {
        NSError *respError = nil;
        NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&respError];
        if (respError == nil) {
            NSString* resp = [dictionary objectForKey:@"result"];
            if(resp != nil && [resp isEqualToString:@"SUCCESS"]) {
                result = YES;
            } else {
                NSLog(@"uploading %@ failed: %@", filename, [dictionary objectForKey:@"message"]);
            }
        }
    }
    */
}


////////////////////////////////////////////////////////

+ (NSData *)createBodyWithBoundary:(NSString *)boundary
                        parameters:(NSDictionary *)parameters
                             paths:(NSArray *)paths
                         fieldName:(NSString *)fieldName
{
    NSMutableData *httpBody = [NSMutableData data];
    
    // add params (all params are strings)
    
    [parameters enumerateKeysAndObjectsUsingBlock:^(NSString *parameterKey, NSString *parameterValue, BOOL *stop) {
        [httpBody appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
        [httpBody appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n", parameterKey] dataUsingEncoding:NSUTF8StringEncoding]];
        [httpBody appendData:[[NSString stringWithFormat:@"%@\r\n", parameterValue] dataUsingEncoding:NSUTF8StringEncoding]];
    }];
    
    // add image data
    
    for (NSString *path in paths) {
        NSString *filename  = [path lastPathComponent];
        NSData   *data      = [NSData dataWithContentsOfFile:path];
        NSString *mimetype  = @"application/octet-stream";
        
        [httpBody appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
        [httpBody appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n", fieldName, filename] dataUsingEncoding:NSUTF8StringEncoding]];
        [httpBody appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\n\r\n", mimetype] dataUsingEncoding:NSUTF8StringEncoding]];
        [httpBody appendData:data];
        [httpBody appendData:[@"\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
    }
    
    [httpBody appendData:[[NSString stringWithFormat:@"--%@--\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    
    return httpBody;
}

+ (NSString *)generateBoundaryString
{
    return [NSString stringWithFormat:@"Boundary-%@", [[NSUUID UUID] UUIDString]];
}

@end
