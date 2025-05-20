let { merge } = require("webpack-merge");
const webpack = require("webpack");

module.exports = (env) => {
    console.log(`env.ENV_FILE is ${env.ENV_FILE ? 'defined: ' : 'undefined'}`, env.ENV_FILE);
    const dotEnv = require('dotenv').config({ path: env.ENV_FILE ? env.ENV_FILE : "./.env" })
    return merge(require("./webpack.common.js"), {
   mode: 'development',
   output: {
      publicPath: "/",
   },
   module: {
      rules: [
         {
            test: /\.s[ac]ss$/i,
            exclude: /node_modules/,
            use: [
               {
                  loader: 'style-loader',
               },
               {
                  loader: 'css-loader',
                  options: { url: false }
               },
               {
                  loader: 'sass-loader',
               },
            ],
         },
      ],
   },
   devtool: 'eval-source-map',
   devServer: {
      historyApiFallback: true,
      client: {
         overlay: false
      },
   },
   plugins: [
      new webpack.DefinePlugin({
         "process.env": JSON.stringify(dotEnv.parsed),
       }),
   ],
})};
