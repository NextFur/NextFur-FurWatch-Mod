import figlet from "figlet"
import chalk from "chalk"

const text = figlet.textSync("FurWatch v1", {
  horizontalLayout: "fitted",
  font: "Big Money-ne"
})

console.clear()
console.log(chalk.blue(("=").repeat(110)))
console.log('\n\n' + chalk.yellow(text) + '\n')