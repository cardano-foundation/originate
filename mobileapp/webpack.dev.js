const webpack = require("webpack");
let { merge } = require("webpack-merge");
const env = require("dotenv").config({ path: "../.env.dev" });

module.exports = merge(require("./webpack.common.js"), {
  mode: "development",
  module: {
    rules: [
      {
        test: /\.s[ac]ss$/i,
        exclude: /node_modules/,
        use: [
          {
            loader: "style-loader",
          },
          {
            loader: "css-loader",
            options: { url: false },
          },
          {
            loader: "sass-loader",
          },
        ],
      },
    ],
  },
  devtool: "eval-source-map",
  devServer: {
    historyApiFallback: true,
    client: {
      overlay: false,
    },
  },
  plugins: [
    new webpack.DefinePlugin({
      "process.env": JSON.stringify(env.parsed),
    }),
  ],
});
