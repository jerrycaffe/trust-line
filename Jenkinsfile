pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "jerrycaffe/trustline:${env.BUILD_NUMBER}"
    RENDER_SERVICE_ID = credentials("render-service-id")
    RENDER_API_KEY = credentials('render-api-key')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        sh './gradlew clean test'
      }
    }

    stage('Build Docker Image') {
      when {
        expression { env.BRANCH_NAME == 'dev' && !env.CHANGE_ID }
      }
      steps {
        sh 'docker build -t $DOCKER_IMAGE .'
      }
    }

    stage('Push Docker Image') {
      when {
        expression { env.BRANCH_NAME == 'dev' && !env.CHANGE_ID }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh '''
            echo $PASS | docker login -u $USER --password-stdin
            docker push $DOCKER_IMAGE
          '''
        }
      }
    }

    stage('Deploy to Render') {
      when {
        expression { env.BRANCH_NAME == 'dev' && !env.CHANGE_ID }
      }
      steps {
        sh '''
          curl -X POST "https://api.render.com/v1/services/${RENDER_SERVICE_ID}/deploys" \
          -H "Authorization: Bearer ${RENDER_API_KEY}" \
          -H "Content-Type: application/json" \
          -d '{"clearCache": true}'
        '''
      }
    }
  }

  post {
    always {
      echo "Build finished: ${currentBuild.result}"
    }
  }
}
