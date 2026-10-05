pipeline {
    agent any
    tools {
        maven 'maven-3.9.16'
    }
    stages {
        stage('test') {
            steps {
                echo 'Testing...'
                sh 'mvn -B -ntp test'
            }
        }

        stage('SCA - dependency scan') {
            steps {
                echo 'Scanning dependencies for known CVEs...'
                sh '''
                    mkdir -p ${WORKSPACE}/reports
                    mkdir -p /var/jenkins_home/.cache/trivy

                    trivy fs \
                    --cache-dir /var/jenkins_home/.cache/trivy \
                    --scanners vuln,secret \
                    --severity HIGH,CRITICAL \
                    --exit-code 1 \
                    -f json \
                    -o ${WORKSPACE}/reports/trivy-sca-report.json \
                    .
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh "${tool 'SonarScanner'}/bin/sonar-scanner \
                    -Dsonar.projectKey=my-app \
                    -Dsonar.projectName='java-maven-app' \
                    -Dsonar.sources=src/main/java \
                    -Dsonar.tests=src/test/java \
                    -Dsonar.exclusions=target/**,**/*.html \
                    -Dsonar.java.binaries=target/classes \
                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                    -Dsonar.host.url=\$SONAR_HOST_URL \
                    -Dsonar.token=\$SONAR_AUTH_TOKEN"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Building the image...'
                sh 'docker build \
                -t localhost:5001/mvn-app:$BUILD_NUMBER \
                -t localhost:5001/mvn-app:latest .'
                sh 'docker push localhost:5001/mvn-app:$BUILD_NUMBER'
                sh 'docker push localhost:5001/mvn-app:latest'
            }
        }

        stage('image scan') {
            steps {
                sh '''
                    mkdir -p ${WORKSPACE}/reports
                    mkdir -p /var/jenkins_home/.cache/trivy

                    trivy image \
                    --cache-dir /var/jenkins_home/.cache/trivy \
                    --scanners vuln \
                    --severity HIGH,CRITICAL \
                    --timeout 20m \
                    --exit-code 1 \
                    -f json \
                    -o ${WORKSPACE}/reports/trivy-report.json \
                    localhost:5001/mvn-app:$BUILD_NUMBER
                '''
            }
        }

        stage('deploy') {
            steps {
                withCredentials([file(credentialsId: 'kind-kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl apply -f k8s/deployment.yaml
                        kubectl apply -f k8s/service.yaml
                        kubectl set image deployment/mvn-app \
                        mvn-app=localhost:5001/mvn-app:$BUILD_NUMBER
                        kubectl rollout status deployment/mvn-app --timeout=120s
                    '''
                }
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'reports/*.json', allowEmptyArchive: true
        }
    }
}