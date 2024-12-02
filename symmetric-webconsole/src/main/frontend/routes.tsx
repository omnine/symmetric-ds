import DashboardView from 'Frontend/views/dashboard/DashboardView.js';
import MainLayout from 'Frontend/views/MainLayout.js';
import { lazy } from 'react';
import { createBrowserRouter, IndexRouteObject, NonIndexRouteObject, useMatches } from 'react-router-dom';

const AboutView = lazy(async () => import('Frontend/views/about/AboutView.js'));
const MonitorsView = lazy(async () => import('Frontend/views/monitors/MonitorsView.js'));
const NodesView = lazy(async () => import('Frontend/views/nodes/NodesView.js'));
const RAView = lazy(async () => import('Frontend/views/recentactivities/RAView.js'));
const BatchesView = lazy(async () => import('Frontend/views/batches/BatchesView.js'));


export type MenuProps = Readonly<{
  icon?: string;
  title?: string;
}>;

export type ViewMeta = Readonly<{ handle?: MenuProps }>;

type Override<T, E> = Omit<T, keyof E> & E;

export type IndexViewRouteObject = Override<IndexRouteObject, ViewMeta>;
export type NonIndexViewRouteObject = Override<
  Override<NonIndexRouteObject, ViewMeta>,
  {
    children?: ViewRouteObject[];
  }
>;
export type ViewRouteObject = IndexViewRouteObject | NonIndexViewRouteObject;

type RouteMatch = ReturnType<typeof useMatches> extends (infer T)[] ? T : never;

export type ViewRouteMatch = Readonly<Override<RouteMatch, ViewMeta>>;

export const useViewMatches = useMatches as () => readonly ViewRouteMatch[];

export const routes: readonly ViewRouteObject[] = [
  {
    element: <MainLayout />,
    handle: { icon: 'null', title: 'Main' },
    children: [
      { path: '/', element: <DashboardView />, handle: { icon: 'dashboard', title: ' Overview' } },
      { path: '/nodes', element: <NodesView />, handle: { icon: 'server', title: ' Nodes' } },
      { path: '/monitors', element: <MonitorsView />, handle: { icon: 'desktop', title: ' Monitors' } },
      { path: '/batches', element: <BatchesView />, handle: { icon: 'exchange', title: ' Batches' } },
      { path: '/recentactivities', element: <RAView />, handle: { icon: 'clock', title: ' Recent Activities' } },
      { path: '/about', element: <AboutView />, handle: { icon: 'file', title: ' About' } },
    ],
  },
];

const router = createBrowserRouter([...routes], {
  basename: "/app",
});
export default router;
