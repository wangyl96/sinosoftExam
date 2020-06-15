let merge = require('webpack-merge')
let prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_API: '"/api"',
  // API_ROOT: '"http://192.168.0.8/api"',
  ENV_CONFIG: '"dev"',
  // 通过环境变量传入租户code
  TENANT_CODE: process.env.TENANT_CODE || '"gitee"'
})
