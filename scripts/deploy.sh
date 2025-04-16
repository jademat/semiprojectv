#!/bin/bash

# 애플리케이션 디렉토리 설정
APP_DIR="/home/$USER/app"
JAR_FILE="$APP_DIR/application.jar"
DEPLOY_LOG="$APP_DIR/deploy.log"
APPLICATION_LOG="$APP_DIR/application.log"

# 애플리케이션 디렉토리 확인
#mkdir -p $APP_DIR
#mv /home/ubuntu/application.jar $APP_DIR

echo "===== Deployment started at $(date) =====" >> $DEPLOY_LOG

# 실행 중인 애플리케이션 PID 확인
CURRENT_PID=$(pgrep -f $JAR_FILE)

# 실행 중인 애플리케이션이 있으면 종료
if [ -z $CURRENT_PID ]; then
    echo "No application is running." >> $DEPLOY_LOG
else
    echo "Kill process: $CURRENT_PID" >> $DEPLOY_LOG
    kill -15 $CURRENT_PID
    sleep 5
fi

# 애플리케이션 실행
echo "Starting application" >> $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APPLICATION_LOG 2>&1 &

# 새 프로세스 ID
NEW_PID=$(pgrep -f $JAR_FILE)
echo "Application started with PID: $NEW_PID" >> $DEPLOY_LOG
echo "===== Deployment completed at $(date) =====" >> $DEPLOY_LOG
