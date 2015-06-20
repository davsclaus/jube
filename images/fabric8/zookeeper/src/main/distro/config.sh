#!/bin/bash
#
#  Copyright 2005-2015 Red Hat, Inc.
#
#  Red Hat licenses this file to you under the Apache License, version
#  2.0 (the "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
#  implied.  See the License for the specific language governing
#  permissions and limitations under the License.
#


function getKey() {
  echo $1 | cut -d "=" -f1
}

function getValue() {
  echo $1 | cut -d "=" -f2
}

function envValue() {
 local ENTRY=`env | grep $1`
 echo `getValue $ENTRY`
}

CURRENT_CLIENT_PORT=`envValue ZK_CLIENT_SERVICE_PORT`
CURRENT_PEER_PORT=`envValue ZK_PEER_SERVICE_PORT`
CURRENT_ELECTION_PORT=`envValue ZK_ELECTION_SERVICE_PORT`
cp $APP_BASE/conf/zoo-common.cfg $APP_BASE/conf/zoo.cfg

echo "dataDir=$APP_BASE/data" >> $APP_BASE/conf/zoo.cfg
echo "dataLogDir=$APP_BASE/log" >> $APP_BASE/conf/zoo.cfg

if [ ! -z "$CURRENT_CLIENT_PORT" ]; then
echo "clientPort=$CURRENT_CLIENT_PORT" >> $APP_BASE/conf/zoo.cfg
else
 echo "clientPort=2181" >> $APP_BASE/conf/zoo.cfg
fi

#Find the server id
SERVER_ID=`envValue ZK_SERVER_ID`
if [ ! -z "$SERVER_ID" ]; then
  echo "$SERVER_ID" > $APP_BASE//data/myid
  #Find the servers exposed in env.
  for i in `echo {1..15}`;do

    HOST=`envValue ZK_PEER_${i}_SERVICE_HOST`
    PEER_PORT=`envValue ZK_PEER_${i}_SERVICE_PORT`
    ELECTION_PORT=`envValue ZK_ELECTION_${i}_SERVICE_PORT`

    if [ "$SERVER_ID" = "$i" ];then
      echo "server.$i=0.0.0.0:$CURRENT_PEER_PORT:CURRENT_$ELECTION_PORT" >> $APP_BASE/conf/zoo.cfg
    elif [ -z "$HOST" ] || [ -z "$PEER_PORT" ] || [ -z "$ELECTION_PORT" ] ; then
      #if a server is not fully defined stop the loop here.
      break
    else 
      echo "server.$i=$HOST:$PEER_PORT:$ELECTION_PORT" >> $APP_BASE/conf/zoo.cfg
    fi

  done
fi
