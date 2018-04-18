//package net.gnu.util;
//
//import java.util.*;
//import java.lang.reflect.*;
//import android.util.*;
//
//public class ObjectDumper {
//	
//	private static final StringBuilder STRING_BUFFER_NULL = new StringBuilder("null");
//    private static final String EMPTY_SPACE = "\t";
//
//	private static String TAG = "ObjectDumper";
//	
//	private Object obj;
//	private int running = 0;;
//	private HashMap<Object, Object> hashCodeMap;
//	
//	public ObjectDumper(Object obj) {
//		this.obj = obj;
//	}
//	
//	public StringBuilder dump() {
//		if (isAllow()) {
//			hashCodeMap = new HashMap<Object, Object>();
//			StringBuilder ret = dump(obj, new StringBuilder());
//			running = 0;
//			return ret;
//		} else {
//			return new StringBuilder(obj + "");
//		}
//	}
//	
//	private synchronized boolean isAllow() {
//		return ++running == 1;
//	}
//
//	private StringBuilder dump(final Object oriObject, final StringBuilder indent) {
//		if (oriObject == null) {
//			return STRING_BUFFER_NULL;
//		}
//		final Class<? extends Object> oriObjectType = oriObject.getClass();
//		//Log.d(TAG, "oriObject: " + oriObjectType.toString() + ".");
//		final StringBuilder sb = new StringBuilder();
//		try {
//			if (oriObject instanceof Number || oriObjectType.isPrimitive() || oriObjectType == String.class
//				|| oriObjectType == Character.class  || oriObjectType == Boolean.class
//				|| oriObjectType == Object.class || oriObjectType == Class.class) {
//				//sb.append(oriObjectType.getName())
//				return new StringBuilder(indent)
//					//.append(": ")
//					.append(oriObject.toString());
//			} else if (oriObject instanceof java.util.Date) {
//				//sb.append(oriObjectType.getName());
//				return new StringBuilder(indent)
//					//.append(": ")
//					.append(
//					Util.DATETIME_FORMAT.format((java.util.Date) oriObject));
//			} else if (hashCodeMap.containsKey(oriObject)) {
////				if (oriObject instanceof java.util.Date) {
////					return sb
////					//.append(oriObjectType.getName())
////						//.append(": ")
////						.append(Util.DATETIME_FORMAT
////								.format((java.util.Date) oriObject));
////				} else {
//				return new StringBuilder(indent).append(oriObject.toString());
////				}
//			} else {
//				hashCodeMap.put(oriObject, null);
//			}
//
//			//if (!PROHIBIT.containsKey(getPackage(oriObject))) {
//			final StringBuilder newIndent = new StringBuilder(indent).append(EMPTY_SPACE);
//			final StringBuilder newNewIndent = new StringBuilder(newIndent).append(EMPTY_SPACE);
//			//int lengthNew = newNewIndent.length();
//			if (oriObject instanceof Map) {
//				int i = 0;
//				final Map map = (Map) oriObject;
//				Set<Map.Entry> collect = map.entrySet();
//				sb.append(oriObjectType).append("(").append(map.size()).append("), hashCode: ")
//					.append(oriObject.hashCode());
//				for (Map.Entry o : collect) {
//					sb.append("\n").append(newIndent).append(++i).append(EMPTY_SPACE).append(
//						dump(o.getKey(), newNewIndent)).append("=").append(
//						dump(o.getValue(), newNewIndent));
//				}
//			} else if (oriObject instanceof Collection) {
//				int i = 0;
//				final Collection collect = (Collection) oriObject;
//				sb.append(oriObjectType).append("(").append(collect.size()).append("), hashCode: ")
//					.append(oriObject.hashCode());
//				for (Object o : collect) {
//					sb.append("\n").append(newIndent).append(++i).append(EMPTY_SPACE).append(
//						dump(o, newNewIndent));
//				}
//			} else if (oriObject instanceof Enumeration) {
//				sb.append(oriObjectType).append(", hashCode: ").append(oriObject.hashCode());
//				final Enumeration enume = (Enumeration) oriObject;
//				int i = 0;
//				while (enume.hasMoreElements()) {
//					sb.append("\n").append(newIndent).append(++i).append(EMPTY_SPACE).append(
//						dump(enume.nextElement(), newNewIndent));
//				}
//			} else if (oriObject instanceof Iterator) {
//				sb.append(oriObjectType).append(", hashCode: ").append(oriObject.hashCode());
//				int i = 0;
//				final Iterator iter = (Iterator) oriObject;
//				while (iter.hasNext()) {
//					sb.append("\n").append(newIndent).append(++i).append(EMPTY_SPACE).append(
//						dump(iter.next(), newNewIndent));
//				}
//			} else if (oriObject instanceof Iterable) {
//				sb.append(oriObjectType).append(", hashCode: ").append(oriObject.hashCode());
//				int i = 0;
//				final Iterator iter = ((Iterable)oriObject).iterator();
//				while (iter.hasNext()) {
//					sb.append("\n").append(newIndent).append(++i).append(EMPTY_SPACE).append(
//						dump(iter.next(), newNewIndent));
//				}
//			} else if (oriObjectType.isArray()) {
//				final Class<?> componentType = oriObjectType.getComponentType();
//				final int length = Array.getLength(oriObject);
//				if (length == 0) {
//					if (componentType.isPrimitive()
//						|| componentType == String.class
//						|| componentType == Character.class
//						|| componentType == Class.class
//						|| componentType == Boolean.class
//						|| componentType == Double.class
//						|| componentType == Float.class
//						|| componentType == Integer.class
//						|| componentType == Long.class
//						|| componentType == Byte.class
//						|| componentType == Short.class
//						) {
//						sb.append(componentType.getSimpleName());
//					} else {
//						sb.append(componentType.getName());
//					}
//					sb.append(" [").append(length).append("] {}\t");
//					sb.append(", hashCode: ").append(oriObject.hashCode());
//				} else {
//					if (componentType == String.class
//						|| componentType == Character.class
//						|| componentType == Class.class
//						|| componentType == Boolean.class
//						) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						Object[] arr = (Object[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(arr[i]);
//						}
//					} else if (componentType == Double.class
//							   || componentType == Float.class
//							   ) {
//						  sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						Object[] arr = (Object[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(Util.DOUBLE_FORMAT.format((arr[i])));
//						}
//					} else if (componentType == Integer.class
//							   || componentType == Long.class
//							   || componentType == Byte.class
//							   || componentType == Short.class
//							   ) {
//						  sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						Object[] arr = (Object[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(Util.INTEGER_FORMAT.format((arr[i])));
//						}
//					} else if (componentType == Boolean.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						boolean[] arr = (boolean[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(arr[i]);
//						}
//					} else if (componentType == Byte.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						byte[] arr = (byte[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(arr[i]);
//						}
//					} else if (componentType == Character.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						char[] arr = (char[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(arr[i]);
//						}
//					} else if (componentType == Double.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						double[] arr = (double[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(Util.DOUBLE_FORMAT.format(arr[i]));
//						}
//					} else if (componentType == Float.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						float[] arr = (float[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(Util.DOUBLE_FORMAT.format(arr[i]));
//						}
//					} else if (componentType == Integer.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						int[] arr = (int[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(Util.INTEGER_FORMAT.format(arr[i]));
//						}
//					} else if (componentType == Long.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						long[] arr = (long[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(Util.INTEGER_FORMAT.format(arr[i]));
//						}
//					} else if (componentType == Short.TYPE) {
//						sb.append(componentType.getSimpleName());
//						sb.append(" [").append(length);
//						sb.append("] {");
//						short[] arr = (short[]) oriObject;
//						sb.append(arr[0]);
//						for (int i = 1; i < length; i++) {
//							sb.append(", ").append(Util.INTEGER_FORMAT.format(arr[i]));
//						}
//					} else {
//						sb.append(componentType.getName()).append(" [").append(length);
//						sb.append("] {");
//						Object[] arr = (Object[]) oriObject;
//						sb.append("\n").append(newNewIndent)
//						.append(dump(arr[0], newNewIndent));
//						for (int i = 1; i < length; i++) {
//							sb.append(", \n")//.append(newNewIndent)
//							.append(dump(arr[i], newNewIndent));
//						}
//					}
//					sb.append("}\t").append(", hashCode: ").append(oriObject.hashCode());
//				}
//			} else {
//				sb.append(oriObjectType.getName());
//				final Method[] methods = oriObjectType.getDeclaredMethods();
//				//Field[] fields = oriObjectType.getDeclaredFields();
//				Class<?> retType = null;
//				String methodName = null;
//				Method method = null;
////				int length = fields.length;
////				for (int j = 0; j < length; j++)
////				{
////					Field field = fields[j];
////					String name = field.getName();
////					Log.d(TAG, "field: " + name);
////					try {
////						Object retTemp = field.get(oriObject);
////						if (!oriObject.equals(retTemp)) {
////							sb.append("\n").append(indent).append(EMPTY_SPACE).append(name).append(
////								": ").append(toString(retTemp, newIndent, hashCodeMap));
////						}
////					} catch (IllegalArgumentException e) {
//////							e.printStackTrace();
////					} catch (IllegalAccessException e) {
//////							e.printStackTrace();
////					}
////				}
//				final int length = methods.length;
//				for (int j = 0; j < length; j++) {
//					method = methods[j];
//					methodName = method.getName();
//					retType = method.getReturnType();
//					if (method.getParameterTypes().length == 0
//					//&& (Modifier.isPublic(method.getModifiers())
//					//|| Modifier.isProtected(method.getModifiers())
//					//)
//						&& (methodName.startsWith("get") || methodName.startsWith("is") 
//					//|| methodName.startsWith("to")
//						)
//					//&& !methodName.startsWith("toString")       
//					//&& retType != Void.TYPE
////						&& !methodName.equals("wait")
////						&& !methodName.equals("notify")
////						&& !methodName.equals("notifyAll")
//					//&& !methodName.equals("clone")
////						&& !methodName.equals("finalize")
//					//&& !methodName.equals("hashCode")
//						&& !methodName.equals("getClass")
//						) {
//						method.setAccessible(true);
//						if (methodName.startsWith("is")) {
//							methodName = methodName.substring(2);
//						} else {
//							methodName = methodName.substring(3);
//						}
//						methodName = methodName.replaceAll("([a-z0-9])([A-Z]+)", "$1 $2");
//						try {
//							if (retType == String.class
//								|| retType == Character.class
//								|| retType == Character.TYPE
//								|| retType == Boolean.class
//								|| retType == Boolean.TYPE
//								|| retType == Object.class
//								|| retType == Class.class
//								|| ((retType.isArray() 
//								|| retType.isInstance(Collection.class)
//								|| retType.isInstance(Iterator.class)
//								|| retType.isInstance(Iterable.class)
//								|| retType.isInstance(Enumeration.class)
//								|| retType.isInstance(Map.class)) 
//								&& retType.getComponentType() == oriObjectType)
//
////								|| retType.isPrimitive()
////								|| retType == Double.class
////								|| retType == Double.TYPE
////								|| retType == Float.class
////								|| retType == Float.TYPE
////								
////								|| retType == Integer.class
////								|| retType == Integer.TYPE
////								|| retType == Long.class
////								|| retType == Long.TYPE
////								|| retType == Byte.class
////								|| retType == Short.class
////								|| retType == Byte.TYPE
////								|| retType == Short.TYPE
//								) {
//								if (oriObject != null) {
//									final Object invoke = method.invoke(oriObject, (Object[])null);
//									sb.append("\n").append(newIndent).append(methodName)
//									//.append(retType.getName())
//										.append(": ")
//										.append(EMPTY_SPACE)
//										.append(invoke);
//								}
//							} else if (retType == Integer.class
//									   || retType == Integer.TYPE
//									   || retType == Long.class
//									   || retType == Long.TYPE
//									   || retType == Byte.class
//									   || retType == Short.class
//									   || retType == Byte.TYPE
//									   || retType == Short.TYPE
//									   ) {
//								if (oriObject != null) {
//									  final Object invoke = method.invoke(oriObject, (Object[])null);
//									  sb.append("\n").append(newIndent).append(methodName)
//									  //.append(retType.getName())
//										.append(": ")
//										  .append(EMPTY_SPACE)
//										  .append(Util.INTEGER_FORMAT.format(invoke));
//								}
//							} else if (retType == Double.class
//									   || retType == Double.TYPE
//									   || retType == Float.class
//									   || retType == Float.TYPE
//									   //|| retType.isPrimitive()
//									   ) {
//								if (oriObject != null) {
//									  final Object invoke = method.invoke(oriObject, (Object[])null);
//									  sb.append("\n").append(newIndent).append(methodName)
//										  //.append(retType.getName())
//										.append(": ")
//										  .append(EMPTY_SPACE)
//										  .append(Util.DOUBLE_FORMAT.format(invoke));
//								}
//							} else {
//								if (oriObject != null) {
//									final Object retTemp = method.invoke(oriObject, (Object[])null);
//									if (retTemp != null && oriObjectType != retTemp.getClass() && !oriObject.equals(retTemp)) {
//										sb.append("\n").append(newIndent).append(methodName).append(
//											": ").append(dump(retTemp, new StringBuilder(newIndent)));
//									}
//								}
//							}
//						} catch (Throwable e) {
//							Log.e(TAG, e.getMessage() + ": " + oriObject + ": " + method);
//						}
//					}
//				}
//			}
////      } else {
////          return new StringBuilder(oriObjectType + " can't not access");
////      }
//		} catch (Throwable e) {
//			Log.e(TAG, oriObject + ": " + e.getMessage());
//		}
//		return sb;
//	}
//}
