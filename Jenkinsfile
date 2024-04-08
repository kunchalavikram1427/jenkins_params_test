import com.sony.sie.cicd.helpers.utilities.JenkinsUtils
import com.sony.sie.cicd.helpers.utilities.GitUtils
import com.sony.sie.cicd.helpers.utilities.ServiceNow
import com.sony.sie.cicd.helpers.utilities.SlackNotifications
import org.codehaus.groovy.GroovyException

def call(def infrastructure = "navigator-cloud") {
    jenkinsUtils = new JenkinsUtils()
    serviceNow = new ServiceNow()
    notifications = new SlackNotifications()
    ansiColor('xterm') {
        try {
            timestamps {
                setProperties()
                process infrastructure
            }
        } catch (GroovyException err) {
            if (!jenkinsUtils.isBuildAborted()) {
                String msg = err.getMessage()
                if (!msg) msg = 'Unknown error!'
                ansi_echo msg, 31
                currentBuild.result = "FAILURE"
            }
        }
    }
}

def process(def infrastructure) {
    def conf = [
        domainName:             params.PSN_DOMAIN,
        infrastructure:         params.INFRASTRUCTURE,
        workload:               params.WORKLOAD,
        repoName:               jenkinsUtils.removeWhiteSpaces(params.REPO_NAME),
        repoBranch:             jenkinsUtils.removeWhiteSpaces(params.REPO_BRANCH),
        helmChartPath:          jenkinsUtils.removeWhiteSpaces(params.HELM_CHART_PATH),
        orgName:                params.ORG_NAME,
        namespacePrefix:        jenkinsUtils.removeWhiteSpaces(params.NAMESPACE_PREFIX),
        deployFromGithub:       params.DEPLOY_FROM_GITHUB,
        aloyGroupsToView:       jenkinsUtils.removeWhiteSpaces(params.ALOY_GROUPS_TO_VIEW),
        aloyGroupsForNonprod:   jenkinsUtils.removeWhiteSpaces(params.ALOY_GROUPS_TO_DEPLOY_NONPROD),
        aloyGroupsForProd:      jenkinsUtils.removeWhiteSpaces(params.ALOY_GROUPS_TO_DEPLOY_PROD),
        deployEnvs:             jenkinsUtils.removeWhiteSpaces(params.DEPLOY_ENVS),
        slackChannels:          jenkinsUtils.removeWhiteSpaces(params.SLACK_CHANNELS),
        helmReleaseName:        jenkinsUtils.removeWhiteSpaces(params.HELM_RELEASE_NAME),
        // postDeploymentTest:     params.POST_DEPLOYMENT_TEST,
        // raaTeamName:            jenkinsUtils.removeWhiteSpaces(params.RAA_TEAM_NAME),
        // raaAppName:             jenkinsUtils.removeWhiteSpaces(params.RAA_APP_NAME),
        raaEnabled:             params.RAA_ENABLED,
        helmChartName:          jenkinsUtils.removeWhiteSpaces(params.CHART_NAME),
        serviceNowConfig:       jenkinsUtils.removeWhiteSpaces(params.SERVICENOW_NAMES),
        serviceNowLegacyPillar: jenkinsUtils.removeWhiteSpaces(params.SERVICENOW_LEGACY_PILLAR),
        serviceDescription:     jenkinsUtils.removeWhiteSpaces(params.SERVICE_DESCRIPTION),
        primaryContact:         jenkinsUtils.removeWhiteSpaces(params.PRIMARY_CONTACT),
        secondaryContact:       jenkinsUtils.removeWhiteSpaces(params.SECONDARY_CONTACT),
        xmattersGroup:          jenkinsUtils.removeWhiteSpaces(params.XMATTERS_GROUP),
        serviceCriticality:     jenkinsUtils.removeWhiteSpaces(params.SERVICE_CRITICALITY)
    ]
    if(params.AUTO_CD_DEPLOY_UPTO != "" && params.AUTO_CD_DEPLOY_UPTO != "NOT APPLICABLE") {
        conf.autoCDTrigger = true
        conf.autoDeployTo = params.AUTO_CD_DEPLOY_UPTO
    }
    
    stage("Input Validation") {
        echo "Starting Jenkins Job..."
        echo "${conf}"
        setProperties()
        if (conf.domainName == "") throw new GroovyException("The Jenkins domain name was not selected, please select.")
        if (conf.infrastructure == "") throw new GroovyException("The infrastructure name was not selected, please select.")
        if (conf.repoName == "") throw new GroovyException("The repo name was not provided, please input.")
        if (conf.repoBranch == "") throw new GroovyException("The repo branch was not provided, please input.")
        if (conf.helmChartPath == "") throw new GroovyException("The helm chart path was not provided, please input.")
        if (conf.namespacePrefix == "") throw new GroovyException("The namespace prefix was not provided, please input.")
        if (conf.aloyGroupsForNonprod == "") throw new GroovyException("The aloy groups for nonprod deployment was not provided, please input.")
        if (conf.aloyGroupsForProd == "") throw new GroovyException("The aloy groups for prod deployment was not provided, please input.")
        if (conf.deployEnvs == "") throw new GroovyException("The deployment line + envs was not selected, please select.")
        if (conf.primaryContact == "") throw new GroovyException("Please enter primary contact for this service.")
        if (conf.serviceDescription == "") throw new GroovyException("Please enter the description, purpose, or function of this service.")
        if (conf.serviceCriticality == "") throw new GroovyException("Please enter the service criticality level.")
        if (conf.xmattersGroup == "") throw new GroovyException("Please enter the Xmatters Group, used to contact group in case of emergency.")
        
        currentBuild.description = "${conf.repoName}"
    }
    env.HELM_CHART_PATH = conf.helmChartPath
    if(conf.helmChartName == "") conf.helmChartName = conf.repoName

    jenkinsUtils.jenkinsNode([infrastructure: infrastructure, templateType: "checkout"]) {
        dir(conf.repoName) {
            container("build-tools"){
                try {
                    stage("Check ServiceNow CI Onboard") {
                        echo "Check ServiceNow CI Requirements"
                        serviceNow.checkPsnAppSysId(conf)
                    }
                } catch (Exception err) {
                    currentBuild.result = "UNSTABLE"
                    echo "Checking ServiceNow CI Requirements failed: ${err.getMessage()}"
                    waitForOverride()
                }
                stage("Create Onboard PR") {
                    echo "Create Onboard PR"
                    createOnboardPR conf
                }
            }
        }
    }
}

def waitForOverride() {
    //Display a gate until user overrides / cancels, or timeout is reached
    timeout(60) {
        String waitingForOverride = "Checking for the ServiceNow CI requirements failed. You will not be able to deploy with traffic enabled to UKS without ServiceNow CI created.\n\n" +
            "[Override] Click \"Override\" to bypass this checking and creating an onboarding PR.\n" +
            "[Abort]: Cancel this onboarding job"
        input id: 'userOverride', ok: "Override", message: waitingForOverride
    }
}

def createOnboardPR(def conf) {
    cleanWs()
    String configRepoName = "engine-cd-configurations"
    jenkinsUtils.checkoutGitSCM(configRepoName, "master", "SIE")
    def confMap = [
        name: conf.helmChartName,
        kind: "eks",
        serviceType: "standard",
        github: "${conf.orgName}/${conf.repoName}",
        infrastructure: conf.infrastructure,
        namespacePrefix: conf.namespacePrefix,
        helmChartPath: conf.helmChartPath,
        deployFromGithub: conf.deployFromGithub
    ]
    //workload
    if(conf.workload != "") confMap.workload = conf.workload
    //helm Release Name
    if(conf.helmReleaseName != "") confMap.helmReleaseName = conf.helmReleaseName
    //slack channels
    if(conf.slackChannels != "") {
        confMap.slackChannels = []
        def slackChannels = conf.slackChannels.split(",")
        for(def channel in slackChannels) {
            confMap.slackChannels.add([name:channel])
        }
    }
    //permission
    confMap.permission = [deploy: [nonprod: [], prod: []]]
    //permission: browse
    if (conf.aloyGroupsToView != "") {
        confMap.permission.browse = []
        def aloyGroups = conf.aloyGroupsToView.split(",")
        for(def group in aloyGroups) {
            confMap.permission.browse.add(group)
        }
    }
    //permission: nonprod
    def aloyGroups = conf.aloyGroupsForNonprod.split(",")
    for(def group in aloyGroups) {
        confMap.permission.deploy.nonprod.add(group)
    }
    //permission: prod
    aloyGroups = conf.aloyGroupsForProd.split(",")
    for(group in aloyGroups) {
        confMap.permission.deploy.prod.add(group)
    }
    //artifactory -- if not deploy from github, artifactory setting is required
    if(!conf.deployFromGithub) confMap.artifactory = [artifactId: conf.helmChartName]
    //auto cd trigger
    if(conf.autoCDTrigger) {
        confMap.autoCDtrigger = [enable: true, deployUpto: conf.autoDeployTo]
    }
    //RAA ready for scan for laco repo
    if(conf.infrastructure == "laco-cloud") {
        confMap.raaEnabled = conf.raaEnabled
    }
    //post deployment test
    // if(conf.postDeploymentTest == "Enable post deployment test") {
    //     def testJobUrl = jenkinsUtils.removeWhiteSpaces(params.AJAX_INTEGRATION_TEST_JOB_URL)
    //     if (testJobUrl == "") throw new GroovyException("The ajax integration test job url is not provided, please input.")
    //     confMap.postDeploymentAjaxIntegrationTest = [
    //         enabled: true,
    //         rollbackDeployment: false,
    //         projectDir: params.PROJECT_DIR == "" ? "./" : params.PROJECT_DIR,
    //         remoteJobUrl: testJobUrl
    //     ]
    // }

    // if(conf.raaTeamName) confMap.raaTeamName = conf.raaTeamName
    // if(conf.raaAppName) confMap.raaAppName = conf.raaAppName

    //serviceNow config names
    if(conf.serviceNowConfig != "") {
        confMap.serviceNowConfig = []
        def configNames = conf.serviceNowConfig.split(",")
        for(def item in configNames) {
            if(item.contains(":")) {
                def arr = item.split(":")
                confMap.serviceNowConfig.add([serviceName: arr[0], serviceNowCIName: arr[1]])
            } else {
                throw new GroovyException("Both the serviceName and serviceNowCIName are required for SERVICENOW_NAMES input and the format is serviceName:serviceNowCIName, please input.")
            }
        }
    }
    //deployEnvs
    confMap.deployEnvs = []
    def deployEnvs = conf.deployEnvs.split(",")
    for(def psenv in deployEnvs) {
        confMap.deployEnvs.add([name: psenv])
    }
    // echo "${confMap}"
    def fileName = "${conf.helmChartName}.yaml"
    def filePath = "${conf.domainName}/cd"
    dir(filePath) {
        if(fileExists(fileName)) sh "rm ${fileName}"
        writeYaml file: fileName, data: confMap
        // sh "ls -la"
        sh "cat ${fileName}"
    }
    String branchName = "${conf.repoName}-onboarding"
    String prTitle = "onboarding ${conf.repoName}"
    String msgBody = "Onboarding ${conf.repoName} to engine cd pipelines on core-jenkins. Make sure to review and test it before merging to master."
    new GitUtils().createPR(configRepoName, "SIE", branchName, ["${filePath}/${fileName}"], prTitle, msgBody)
}

def setProperties(){
    // def orgList = ["SIE", "SIE-PRIVATE"]
    def domainList = ["", "coreexperience", "dataplatform", "gaminglife", "monetization", "partnersplatform", "pci", "platformfundamentals", "psdirect"]
    // def infrastructureList = ["", "kamaji-cloud", "laco-cloud", "navigator-cloud", "roadster-cloud"]
    def pillarList = ["", "Roadster", "Kamaji", "Laco", "Navigator"]
    def testList = ["", "Enable post deployment test"]
    def serviceCriticaity = ["", "Low", "Medium", "High"]
    def enableTestList = "\'"+testList.join("\', \'")+"\'"
    
    def settings = [
        choice(
                choices: domainList.join("\n"),
                description: 'Required: PSN Domain name, i.e.: coreexperience',
                name: 'PSN_DOMAIN'
        ),
        choice(
                choices: infrastructureList.join("\n"),
                description: 'Required: infrastructure name, i.e.: navigator-cloud',
                name: 'INFRASTRUCTURE'
        ),
        choice(
                choices: ["", "k8s-service", "k8s-cron"].join("\n"),
                description: 'Optional: For batch deployment, please select k8s-cron',
                name: 'WORKLOAD'
        ),
        string(
                defaultValue: '',
                description: 'Required: Github repository name, i.e.: poki-simple',
                name: 'REPO_NAME'
        ),
        string(
                defaultValue: '',
                description: 'Required: GitHub branch with Unified Chart files i.e.: main',
                name: 'REPO_BRANCH'
        ),
        string(
                defaultValue: '',
                description: 'Required: Folder path in git repo of unified Chart File, i.e.: impl/helm-unified/poki-simple',
                name: 'HELM_CHART_PATH'
        ),
        choice(
                choices: orgList.join("\n"),
                description: 'Required: Github organization name, i.e.: SIE',
                name: 'ORG_NAME'
        ),
        string(
                defaultValue: '',
                description: "Optional: helm chart name and the default value is repo name, i.e.: poki-simple",
                name: 'CHART_NAME'
        ),
        string(
                defaultValue: '',
                description: 'Required: the prefix of k8s namesplace on the global level, i.e. if the namespace is poki-simple-e1np, the namespacePrefix will be poki-simple',
                name: 'NAMESPACE_PREFIX'
        ),
        string(
                defaultValue: '',
                description: "Optional: helm release name like 'helm install HELM_RELEASE_NAME ...', if it is not defined, the repo name will be used. i.e.: poki-simple",
                name: 'HELM_RELEASE_NAME'
        ),
        booleanParam(
            defaultValue: false,
            description: 'Optional: Support helm deployment from github',
            name: 'DEPLOY_FROM_GITHUB'
        ),
        string(
                defaultValue: '',
                description: 'Optional: Aloy groups to view the CD jobs [use comma to seperate groups], i.e.: PSN-CoreJenkinsProd-SSECare',
                name: 'ALOY_GROUPS_TO_VIEW'
        ),
        string(
                defaultValue: '',
                description: 'Required: Aloy groups with Jenkins jobs view, build and has nonprod deployment permissions [use comma to seperate groups], i.e.: PSN-CoreJenkinsNprd-EngEnable',
                name: 'ALOY_GROUPS_TO_DEPLOY_NONPROD'
        ),
        string(
                defaultValue: '',
                description: 'Required: Aloy groups with Jenkins jobs view, build, configure, and has prod and nonprod deployment permissions [use comma to seperate groups], i.e.: PSN-CoreJenkinsNprd-EngEnable',
                name: 'ALOY_GROUPS_TO_DEPLOY_PROD'
        ),
        string(
                defaultValue: '',
                description: 'Required: Primary Contact for this Service - service owner, or team leads email - used for ServiceNow Onboarding, i.e.: jun.yu@sony.com',
                name: 'PRIMARY_CONTACT'
        ),
        string(
                defaultValue: '',
                description: 'Optional: Secondary Contact for this Service - service owner, or team leads email - used for ServiceNow Onboarding, i.e.: gerry.soulos@sony.com',
                name: 'SECONDARY_CONTACT'
        ),
        string(
                defaultValue: '',
                description: 'Required: Enter XMATTER Group, which will be used to contact service team in case of emergency. - used for ServiceNow Onboarding, i.e.: gerry.soulos@sony.com',
                name: 'XMATTERS_GROUP'
        ),
        string(
                defaultValue: '',
                description: 'Required: Description, purpose, or function of this Service. - used for Service Now Onboarding, i.e.: poki-simple is a deployment testapp',
                name: 'SERVICE_DESCRIPTION'
        ),        
        choice(
                choices: pillarList.join("\n"),
                description: 'Optional: If team used previous ServiceNow, what pillar? - used for Service Now Onboarding, i.e.: Roadster, Kamaji, Laco, Navigator',
                name: 'SERVICENOW_LEGACY_PILLAR'
        ),
        choice(
                choices: serviceCriticaity.join("\n"),
                description: 'Required: How would a service outage impact the PlayStation network? - used for Service Now Onboarding , i.e.: Low',
                name: 'SERVICE_CRITICALITY'
        ),
        // string(
        //         defaultValue: '',
        //         description: "Required: Chart name of the Chart.yaml, i.e. poki-simple",
        //         name: 'ARTIFACT_ID'
        // ),
        // string(
        //         defaultValue: '',
        //         description: 'Optional: repository in the engine-helm-virtual artifactory, i.e. engine-helm-virtual/poki-simple',
        //         name: 'ARTIFACTORY_REPOSITORY'
        // ),
        string(
                defaultValue: '',
                description: 'Optional: Slack team channels [use comma to separate channels], i.e.: poki-simple',
                name: 'SLACK_CHANNELS'
        ),
        // string(
        //         defaultValue: '',
        //         description: 'Optional: RAA team name. i.e. korra',
        //         name: 'RAA_TEAM_NAME'
        // ),
        // string(
        //         defaultValue: '',
        //         description: 'Optional: RAA App Name and its default value is the repo name. i.e. poki-simple',
        //         name: 'RAA_APP_NAME'
        // ),
        string(
                defaultValue: '',
                description: 'Optional: Pair with serviceName and serviceNowCIName, format: serviceName:serviceNowCIName [use comma to separate config names] . i.e. Haste_Sample:Haste_Sample-psn',
                name: 'SERVICENOW_NAMES'
        ),
        booleanParam(
            defaultValue: false,
            description: 'Applicable for LACO repo only and if checked: RAA is ready to scan.',
            name: 'RAA_ENABLED'
        )
    ]

    def kmjDeployList = ["", "E1-PMGT", "E1-NP", "MGMT_PQA_SPINT", "P1-NP"]
    def psenvList = "\'"+kmjDeployList.join("\', \'")+"\'"
    settings.add([$class: 'CascadeChoiceParameter',
        name: 'AUTO_CD_DEPLOY_UPTO',
        description: 'KAMAJI CLOUD ONLY: the last environment to deploy to: i.e. E1-NP',
        choiceType: 'PT_SINGLE_SELECT',
        filterLength: 1,
        filterable: false,
        randomName: 'choice-parameter-01',
        referencedParameters: 'INFRASTRUCTURE',
        script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: true, script: "return [' NOT APPLICABLE ']"],
                script: [classpath: [], sandbox: true, script: """
                if (INFRASTRUCTURE.equals("kamaji-cloud")) {
                    return [${psenvList}]
                } else {
                    return ['NOT APPLICABLE']
                }"""]
        ]
    ])

    def kmjEnvList = "\'"+createCDJob.getAllPsenvList("kamaji-cloud").join("\', \'")+"\'"
    def lacoEnvList = "\'"+createCDJob.getAllPsenvList("laco-cloud").join("\', \'")+"\'"
    def navEnvList = "\'"+createCDJob.getAllPsenvList("navigator-cloud").join("\', \'")+"\'"
    def rdsEnvList = "\'"+createCDJob.getAllPsenvList("roadster-cloud").join("\', \'")+"\'"
    settings.add([$class: 'CascadeChoiceParameter',
        name: 'DEPLOY_ENVS',
        description: 'Required: deployment line + envs, i.e.: e1-np, p1-np',
        choiceType: 'PT_CHECKBOX',
        filterLength: 1,
        filterable: false,
        randomName: 'choice-parameter-02',
        referencedParameters: 'INFRASTRUCTURE',
        script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: true, script: "return [' NOT APPLICABLE ']"],
                script: [classpath: [], sandbox: true, script: """
                if (INFRASTRUCTURE.equals("kamaji-cloud")) {
                    return [${kmjEnvList}]
                } else if (INFRASTRUCTURE.equals("laco-cloud")) {
                    return [${lacoEnvList}]
                } else if (INFRASTRUCTURE.equals("navigator-cloud")) {
                    return [${navEnvList}]
                } else if (INFRASTRUCTURE.equals("roadster-cloud")) {
                    return [${rdsEnvList}]
                } else {
                    return ['NOT APPLICABLE']
                }"""]
        ]
    ])
     
    properties([
        buildDiscarder(
            logRotator(
                artifactDaysToKeepStr: '',
                artifactNumToKeepStr: '',
                daysToKeepStr: '30',
                numToKeepStr: '1000')
        ),
        parameters(settings),
        pipelineTriggers([])
    ])
}

void ansi_echo(String txt, Integer color = 34) {
    //color code: black: 30, red: 31, green: 32, yellow: 33, blue: 34, purple: 35
    echo "\033[01;${color}m ${txt}...\033[00m"
}
