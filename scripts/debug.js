import env from './utils/enviropment.js'
import print from './utils/logger.js'
import './utils/header.js'

import { Launch, Mojang } from 'minecraft-java-core'
import cliProgress from 'cli-progress'
import pw from 'node-process-windows'
import { execa } from 'execa'
import chalk from 'chalk'
import axios from 'axios'
import path from 'path'
import fs from 'fs'

print.info('Initializing debug mode...')
print.info('Setted environment variables:')

Object.entries(env).forEach(([key, value]) => {
  print.info(chalk.gray(`- ${chalk.blue(key)} > ${chalk.yellow(value)}`))
})

const __dirname = path.resolve(new URL(import.meta.url).hostname)
const debugDir = path.join(__dirname, 'debug')

const serverDir = path.join(debugDir, env.PROJECT_SERVER_DIR)
const clientDir = path.join(debugDir, env.PROJECT_CLIENT_DIR)

const serverMaxRAM = env.SERVER_MAX_HEAP_SIZE
const clientMaxRAM = env.CLIENT_MAX_HEAP_SIZE
const serverMinRAM = env.SERVER_MIN_HEAP_SIZE
const clientMinRAM = env.CLIENT_MIN_HEAP_SIZE

const MinecraftVersion = env.CLIENT_MINECRAFT_VERSION
const NeoForgeVersion = env.CLIENT_NEOFORGE_VERSION

print.info('Checking if debug folder exists...')

if(!fs.existsSync(debugDir)) {
  fs.mkdirSync(debugDir, { recursive: true })
  print.success('Debug folder created successfully.')
}

if(!fs.existsSync(serverDir)) {
  fs.mkdirSync(serverDir, { recursive: true })
  print.success('Server directory created successfully.')
}

if(!fs.existsSync(clientDir)) {
  fs.mkdirSync(clientDir, { recursive: true })
  print.success('Client directory created successfully.')
}

const serverIsEmpty = fs.globSync(`${serverDir}/*`).length === 0
print.info('Debug environment created successfully.')
print.info('Verifying server & client installation...')

if(serverIsEmpty) {
  await new Promise(async (resolve) => {
    print.info('Server directory is empty. Installing server...')

    const youerAPI = `https://api.mohistmc.com/project/youer/${MinecraftVersion}/builds`
    const youerBuilds = (await axios.get(youerAPI)).data

    if(!youerBuilds || youerBuilds.length === 0) {
      print.error('Failed to fetch Youer builds. Please check your internet connection or the API status.')
      process.exit(1)
    }

    const youerFilteredBuilds = youerBuilds.filter(build => build.loader.neoforge_version === NeoForgeVersion)
    const latestYouerBuild = youerFilteredBuilds.sort((a, b) => new Date(b.build_date) - new Date(a.build_date))[0]
    const youerBuild = `https://api.mohistmc.com/project/youer/${MinecraftVersion}/builds/${latestYouerBuild.id}/download`
    
    const youerFileName = `youer-${MinecraftVersion}-${NeoForgeVersion}.jar`
    const youerFilePath = path.join(serverDir, youerFileName)
    const youerFile = fs.createWriteStream(youerFilePath)

    const youerResponse = await axios.get(youerBuild, { responseType: 'stream' })
    const youerFileSize = youerResponse.headers['content-length'] || 117747663

    const youerBar = new cliProgress.SingleBar({
      format: `${chalk.green('[PROGRESS]')} Downloading Youer server: ${chalk.cyan('{bar}')} | {percentage}% | {value}/{total} bytes`,
      barCompleteChar: chalk.green('█'),
      barIncompleteChar: chalk.red('░'),
      hideCursor: true
    })

    youerBar.start(youerFileSize, 0)
    youerResponse.data.pipe(youerFile)

    youerResponse.data.on('data', (chunk) => {
      youerBar.increment(chunk.length)
    })

    youerFile.on('finish', () => {
      youerBar.stop()
      youerFile.close()
      print.success(`Youer server downloaded successfully.`)

      fs.writeFileSync(path.join(serverDir, 'server.properties'), `online-mode=false\nserver-port=${env.SERVER_PORT}\nserver-ip=${env.SERVER_HOST}`, 'utf8')
      
      resolve()
    })

    youerFile.on('error', (err) => {
      youerBar.stop()
      youerFile.close()
      print.error(`Failed to download Youer server: ${err.message}`)
      process.exit(1)
    })
  })
}

if(!fs.existsSync(path.join(serverDir, 'eula.txt'))) {
  fs.writeFileSync(path.join(serverDir, 'eula.txt'), 'eula=true', 'utf8')
}

print.info('Compiling & Importing Plugin...')

const pluginsDir = path.join(serverDir, 'plugins')
if(!fs.existsSync(pluginsDir)) fs.mkdirSync(pluginsDir, { recursive: true })

const pluginProcess = await execa('../gradlew', ['assemble', '--parallel', '--build-cache'], {
  cwd: path.join(__dirname, 'plugin')
})

if(pluginProcess.failed) {
  print.error('Failed to compile plugin with Gradlew.')
  process.exit(1)
}

const pluginJar = fs.globSync(path.join(__dirname, 'plugin', 'build', 'libs', '*.jar'))[0]
fs.copyFileSync(pluginJar, path.join(pluginsDir, 'FurWatch.jar'))
fs.unlinkSync(pluginJar)

print.success('Plugin compiled and imported successfully.')
print.info('Compiling & Importing Mod...')

const modsDir = path.join(clientDir, 'mods')
if(!fs.existsSync(modsDir)) fs.mkdirSync(modsDir, { recursive: true })

const modProcess = await execa('../gradlew', ['assemble', '--parallel', '--build-cache'], {
  cwd: path.join(__dirname, 'mod')
})

if(modProcess.failed) {
  print.error('Failed to compile mod with Gradlew.')
  process.exit(1)
}

const modJar = fs.globSync(path.join(__dirname, 'mod', 'build', 'libs', '*.jar'))[0]
fs.copyFileSync(modJar, path.join(modsDir, 'FurWatch.jar'))
fs.unlinkSync(modJar)

print.success('Mod compiled and imported successfully.')
print.info('Starting server...')

const serverProcess = await execa('wt', [
  'new-tab', '--title', 'FurWatch Server', '-c', 'cmd.exe',
  '/k', `cd /d "${serverDir}" && java -Xms${serverMinRAM} -Xmx${serverMaxRAM} -jar youer-${MinecraftVersion}-${NeoForgeVersion}.jar nogui`
])

if(serverProcess.failed) {
  print.error('Failed to start server.')
  process.exit(1)
}

print.success('Server started successfully.')
print.info('Starting client...')

const launcher = new Launch()
const auth = await Mojang.login('dev')

launcher.on('data', line => {
  process.stdout.write('\n'+chalk.gray(`${chalk.cyan('[STDOUT]')} ${line}`))
})

launcher.on('close', () => {
  pw.getProcesses((_, processes) => {
    processes.forEach(process => {
      if(process.mainWindowTitle.includes('FurWatch Server')) {
        execa('taskkill', ['/F', '/PID', process.pid.toString()])
          .then(() => print.success(`Process ${process.mainWindowTitle} (PID: ${process.pid}) killed successfully.`))
          .catch(err => print.error(`Failed to kill process ${process.mainWindowTitle} (PID: ${process.pid}): ${err.message}`))
      }
    })
  })

  print.info('Game exited.')
})

await launcher.Launch({
  version: MinecraftVersion,
  authenticator: auth,
  path: clientDir,
  bypassOffline: true,
  loader: {
    type: 'neoforge',
    build: NeoForgeVersion,
    enable: true
  },
  memory: {
    max: clientMaxRAM,
    min: clientMinRAM
  }
})