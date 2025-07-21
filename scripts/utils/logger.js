import chalk from "chalk"

export default class Logger {
  static log(message) {
    console.log(chalk.blue(`[LOG] ${chalk.white(message)}`))
  }

  static info(message) {
    console.info(chalk.cyan(`[INFO] ${chalk.white(message)}`))
  }

  static warn(message) {
    console.warn(chalk.yellow(`[WARN]: ${chalk.white(message)}`))
  }

  static error(message) {
    console.error(chalk.red(`[ERROR]: ${chalk.white(message)}`))
  }

  static debug(message) {
    console.debug(chalk.blue(`[DEBUG]: ${chalk.white(message)}`))
  }

  static success(message) {
    console.log(chalk.green(`[SUCCESS]: ${chalk.white(message)}`))
  }
}