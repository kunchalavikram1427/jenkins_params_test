def resources = ["aws-s3-bucket","aws-irsa","asm","aws-dynamodb", "aws-iam-policy"]
def env_line = ["e1np","mldev","e1pmgt","sandbox"]

properties([
    parameters([
        choice(
          choices: env_line.join("\n"),
          description: 'Select the Environment Line from the Dropdown List',
          name: 'ENVIRONMENT_LINE'
        ),
        choice(
          choices: resources.join("\n"),
          description: 'Select the Resource Name from the Dropdown List',
          name: 'RESOURCE_NAME'
        ),
        string(
          defaultValue: '', 
          name: 'BUCKET_NAME',
          description: 'Enter the bucket name', 
          trim: true
        ),
        booleanParam(
          defaultValue: true, 
          description: 'Enable/disable versioning', 
          name: 'ENABLE_VERSIONING'
        ),
        booleanParam(
          defaultValue: false, 
          description: 'Enable/disable force destroy', 
          name: 'FORCE_DESTROY'
        )
    ])
])
