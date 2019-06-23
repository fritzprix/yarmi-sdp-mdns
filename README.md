# SDP-MDNS  
> Service Discovery Module based on Multicast DNS for YARMI

## How-To
1. Add to your project 
    - Maven 
        ```xml
        <repositories>
            <repository>
                <id>yarmi-core</id>
                <name>yarmi</name>
                <releases>
                    <enabled>true</enabled>
                </releases>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
                <url>https://raw.githubusercontent.com/fritzprix/yarmi/releases</url>
            </repository>
        </repositories>
        <dependencies>
                <dependency>
                    <groupId>net.doodream</groupId>
                    <artifactId>yarmi-core</artifactId>
                    <version>0.1.1</version>
                </dependency>
                <dependency>
                    <groupId>net.doodream.yarmi</groupId>
                    <artifactId>sdp-mdns</artifactId>
                    <version>0.1.1</version>
                </dependency>
                <dependency>
                     <groupId>org.jmdns</groupId>
                     <artifactId>jmdns</artifactId>
                     <version>3.5.1</version>
                </dependency>
        </dependencies>
        ```
    - Gradle
        ```groovy
        allprojects {
            repositories {
                maven {
                    url 'https://raw.githubusercontent.com/fritzprix/yarmi/releases'
                }
                maven {
                    url 'https://raw.githubusercontent.com/fritzprix/yarmi/snapshots'
                }
            }
        }
        dependencies {
            compile 'net.doodream:yarmi-core:0.1.1'
            compile 'net.doodream.yarmi:sdp-mdns:0.1.1'
            compile 'org.jmdns:jmdns:3.5.1'
        }
        ```

