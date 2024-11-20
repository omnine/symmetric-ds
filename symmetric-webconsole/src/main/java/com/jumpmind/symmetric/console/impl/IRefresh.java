package com.jumpmind.symmetric.console.impl;

import org.jumpmind.symmetric.ISymmetricEngine;

public interface IRefresh<T> {
   T onBackgroundDataRefresh(ISymmetricEngine var1);

   void onBackgroundUIRefresh(T var1);

   void onUIError(Throwable var1);
}
