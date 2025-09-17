# Noxesium External Paper API

This module implements an external Paper API that can be used by plugins that do not directly implement Noxesium to access Noxesium client
provided information such as installed mods.

This module should be shaded into your project, it will gracefully detect if Noxesium is present or not and fetch the data if it is.