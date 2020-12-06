package org.mallen.test.common.utils;

import net.sf.cglib.core.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于cglib框架实现bean的拷贝，如果有特殊类型的转换，可以自定义Converter，比如：
 * <pre class="code">
 * {@literal /**}
 *  * 该方法不会传入没有setter和getter方法的属性
 *  *
 *  * @param value   源对象属性值
 *  * @param target  目标对象属性的类
 *  * @param context 目标对象setter方法名
 *  * @return 转换后，被set到目标类属性的值
 *  {@literal *}/
 * public Object convert(Object value,Class target,Object context){
 *   if(value instanceof List && filedName.equals("orders")){
 *      return((List)value).get(0);
 *   }
 *
 *   return value;
 * }
 *</pre>
 * @author mallen
 * @date 10/24/18
 */
public class BeanCopyUtil {
    private static Logger logger = LoggerFactory.getLogger(BeanCopyUtil.class);

    public BeanCopyUtil() {
    }

    public static void copyProperties(Object source, Object target, Converter converter) {
        try {
            boolean useConvert = converter != null;
            net.sf.cglib.beans.BeanCopier copier = net.sf.cglib.beans.BeanCopier.create(source.getClass(), target.getClass(), useConvert);
            copier.copy(source, target, converter);
        } catch (Exception var5) {
            logger.error("", var5);
        }

    }

    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, (Converter)null);
    }
}
