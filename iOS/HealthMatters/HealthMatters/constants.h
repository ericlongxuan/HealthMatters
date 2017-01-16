//
//  constants.h
//  StudentLife
//
//  Created by Rui Wang on 8/20/15.
//  Copyright (c) 2015 Rui Wang. All rights reserved.
//

#ifndef StudentLife_constants_h
#define StudentLife_constants_h

#define SP_USERNAME         @"SP_USERNAME"
#define SP_PASSWORD         @"SP_PASSWORD"
#define SP_ISREGISTERED     @"SP_ISREGISTERED"
#define LAST_ASKED_INFO_Q   @"LAST_ASKED_INFO_Q"
#define LAST_ASKED_INFO_TIME    @"LAST_ASKED_INFO_TIME"
#define DAY_COUNT_INFO      @"DAY_COUNT_INFO"
#define LAST_ASKED_POLL_Q   @"LAST_ASKED_POLL_Q"
#define DAY_COUNT_POLL      @"DAY_COUNT_POLL"
#define POLL_RESPONSE       @"POLL_RESPONSE"
#define ANSWERED_POLL       @"ANSWERED_POLL"

#define TIME_INTERVAL   2*3600
#define INFO_DAY_LIMIT  2
#define POLL_DAY_LIMIT  1


#define EVENT_ID_BEACON_REGION   1
#define EVENT_ID_INFORMATION    2
#define EVENT_ID_POLL           3
#define EVENT_ID_POLL_FEEDBACK  4

#define EMA_URL             @"http://biorhythm.cs.dartmouth.edu:8080/studentlife/studentlife_ema.json"
#define EMA_DATA            @"EMA_DATA"
#define EMA_QUES            @"EMA_QUES"
#define ASK_EMA             @"ASK_EMA"
#endif
