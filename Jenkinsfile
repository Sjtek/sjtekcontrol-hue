#!groovy

node {

    stage('Checkout') {
        checkout scm
    }

    stage('Build') {
    	sh './gradlew jar'
    	archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
    }
}