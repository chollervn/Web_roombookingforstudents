@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d D:\Pjxc\OOP_BTL

call mvnw.cmd spring-boot:run

