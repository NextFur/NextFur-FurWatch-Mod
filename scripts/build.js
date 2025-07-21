import env from './utils/enviropment.js'
import print from './utils/logger.js'
import { execa } from 'execa'
import './utils/header.js'
import path from 'path'
import fs from 'fs'

const __dirname = path.resolve(new URL(import.meta.url).hostname)
const distDir = path.join(__dirname, env.PROJECT_OUTPUT_DIR)

print.info('Building plugin...')

const pluginProcess = await execa('../gradlew', ['build'], {
  cwd: path.join(__dirname, 'plugin')
})

if(pluginProcess.failed) {
  print.error('Failed to compile plugin with Gradlew.')
  process.exit(1)
}

print.info('Building mod...')

const modProcess = await execa('../gradlew', ['build'], {
  cwd: path.join(__dirname, 'mod')
})

if(modProcess.failed) {
  print.error('Failed to compile mod with Gradlew.')
  process.exit(1)
}

print.success('Plugin and mod built successfully.')
print.info('Exporting plugin and mod...')

if(!fs.existsSync(distDir)) {
  fs.mkdirSync(distDir, { recursive: true })
}

const pluginJar = fs.globSync(path.join(__dirname, 'plugin', 'build', 'libs', '*.jar'))[0]
const modJar = fs.globSync(path.join(__dirname, 'mod', 'build', 'libs', '*.jar'))[0]

fs.copyFileSync(pluginJar, path.join(distDir, 'FurWatch-plugin.jar'))
fs.copyFileSync(modJar, path.join(distDir, 'FurWatch-mod.jar'))
fs.unlinkSync(pluginJar)
fs.unlinkSync(modJar)

print.success('Plugin and mod exported successfully.')