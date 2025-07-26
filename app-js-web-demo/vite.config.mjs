import { defineConfig } from "vite";
import { spawn } from "child_process";
// Utility to invoke a given sbt task and fetch its output
function scalaJSPlugin(projectID) {
  let isDev = undefined;
  let scalaJSOutputDir = undefined;
  return {
    name: "scalajs:sbt-scalajs-plugin",
    // Vite-specific
    configResolved(resolvedConfig) {
      isDev = resolvedConfig.mode === 'development';
    },
    // standard Rollup
    async buildStart(options) {
      if (isDev === undefined)
        throw new Error("configResolved must be called before buildStart");
      const task = isDev && projectID ? `${projectID}/fastLinkJSOutput`
        : isDev ? "fastLinkJSOutput"
        : projectID ? `${projectID}/fullLinkJSOutput`
        : "fullLinkJSOutput";
      const args = ["--batch", "-no-colors", "-Dsbt.supershell=false", `print ${task}`];
      const spawnOptions = {
          cwd: "../",
          stdio: ['ignore', 'pipe', 'inherit'],
        }
      const child = process.platform === 'win32'
        ? spawn("sbt", args.map(x => `"${x}"`), {shell: true, ...spawnOptions})
        : spawn("sbt", args, spawnOptions);
      let fullOutput = '';
      child.stdout.setEncoding('utf-8');
      child.stdout.on('data', data => {
        fullOutput += data;
        process.stdout.write(data); // tee on my own stdout
      });
      scalaJSOutputDir = await new Promise((resolve, reject) => {
        child.on('error', err => {
          reject(new Error(`sbt invocation for Scala.js compilation could not start. Is it installed?\n${err}`));
        });
        child.on('close', code => {
          if (code !== 0)
            reject(new Error(`sbt invocation for Scala.js compilation failed with exit code ${code}.`));
          else
            console.log(`${fullOutput}:fullOutput`);
            resolve(fullOutput.trimEnd().split('\n').at(-1));
        });
      });
    },
    // standard Rollup
    resolveId(source, importer, options) {
      if (scalaJSOutputDir === undefined) throw new Error("buildStart must be called before resolveId");
      const fullURIPrefix = "scalajs:";
      if (!source.startsWith(fullURIPrefix)) return null;
      const path = source.substring(fullURIPrefix.length);
      return `${scalaJSOutputDir}/${path}`;
    },
  };
}
export default defineConfig({
  plugins: [scalaJSPlugin("app-js-web-demo")],
});