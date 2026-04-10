node {
    try {
		def aarPath = 'sar-middleware/aar/', version = '1.0.0';
		
        properties([
            buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '10', numToKeepStr: '2')), 
            disableConcurrentBuilds()
            ])
			
        stage('Preparation') {
        	checkout([$class: 'GitSCM', branches: [[name: '*/${BRANCH_NAME}']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'git-production-cct', url: 'https://production.eng.it/gitlab/WSO2_INT/sar-piemonte.git']]])
        }

		stage('POM') {
            def pom = readMavenPom file: 'sar-middleware/pom.xml'
            version = pom.version
            echo "version: ${version}"
        } 
		
        stage('Build cil-wsstubs') {
            def mvnHome = tool name: 'Maven 3.6.0', type: 'maven'
            def javaHome = tool name: 'JDK 1.8', type: 'jdk'
            withEnv(["MVN_HOME=$mvnHome","JAVA_HOME=$javaHome"]) {
             if (isUnix()) {
                sh '"$MVN_HOME/bin/mvn" clean deploy -f ./cil-wsstubs/pom.xml -DskipTests -U'
             }
            }
        }
        stage('Build sapiemonte-mw-skeleton') {
            def mvnHome = tool name: 'Maven 3.6.0', type: 'maven'
            def javaHome = tool name: 'JDK 1.8', type: 'jdk'
            withEnv(["MVN_HOME=$mvnHome","JAVA_HOME=$javaHome"]) {
             if (isUnix()) {
                sh '"$MVN_HOME/bin/mvn" clean deploy -f ./sapiemonte-mw-skeleton/pom.xml -DskipTests -U'
             }
            }
        }		
        stage('Build sar-mw-piemonte') {
            def mvnHome = tool name: 'Maven 3.6.0', type: 'maven'
            def javaHome = tool name: 'JDK 1.8', type: 'jdk'
            withEnv(["MVN_HOME=$mvnHome","JAVA_HOME=$javaHome"]) {
             if (isUnix()) {
                sh '"$MVN_HOME/bin/mvn" clean deploy -f ./sar-mw-piemonte/pom.xml -DskipTests -U'
             }
            }
        }
        stage('Build sar-middleware') {
            def mvnHome = tool name: 'Maven 3.6.0', type: 'maven'
            def javaHome = tool name: 'JDK 1.8', type: 'jdk'
            withEnv(["MVN_HOME=$mvnHome","JAVA_HOME=$javaHome"]) {
             if (isUnix()) {
                sh '"$MVN_HOME/bin/mvn" clean deploy -f ./sar-middleware/pom.xml -DskipTests -U -Pnovara'
             }
            }
        }
		
        stage('Install it.eng.cct.dem.sar.lombardia.siss.aar piemonte') {
            def mvnHome = tool name: 'Maven 3.6.0', type: 'maven'
            def javaHome = tool name: 'JDK 1.8', type: 'jdk'
            withEnv(["MVN_HOME=$mvnHome","JAVA_HOME=$javaHome","VERSION=$version", "ARTIFACT=sar-middleware", "AAR_PATH=$aarPath"]) {			  
			  sh '$MVN_HOME/bin/mvn dependency:get -U --update-snapshots -Dartifact=it.eng.cct.dem.sar.piemonte:$ARTIFACT:$VERSION:aar -DremoteRepositories=http://161.27.202.100:8081 -Ddest=$AAR_PATH$ARTIFACT.aar'

              sh 'mkdir $AAR_PATH$ARTIFACT'
			  
			  sh 'unzip $AAR_PATH$ARTIFACT.aar -d $AAR_PATH$ARTIFACT'
			  
			  sh 'rm -rf $AAR_PATH$ARTIFACT.aar'			  
            }
        }
		
        stage('Docker') {
            if (env.BRANCH_NAME ==~ /(dev|master|test)/)  {       
	            def dockerHome = tool name: 'Docker', type: 'org.jenkinsci.plugins.docker.commons.tools.DockerTool'
	            withEnv(["DOCKER_HOME=dockerHome"]) {
	                def registry = '161.27.202.100:8103'
					def versionImage = '1.0.1'
	                docker.withRegistry("http://$registry", 'nexus') {
	                   
	                    def customImage = docker.build("$registry/healtheng/it.eng.cct.sar-mw-piemonte-axis2:$versionImage", "-f Dockerfile sar-middleware");
	                    try { 
	                        customImage.push();
	                    } finally {
	                        sh "docker rmi ${customImage.imageName()}"
	                    }
	                }
	            }
	        } else {
	            echo 'Docker push skipped! '  
	        }

        }  
    } finally {
        cleanWs notFailBuild: true;
    }
}
